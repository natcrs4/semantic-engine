package com.crs4.sem.rest;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.crs4.sem.config.SemEngineConfig;
import com.crs4.sem.exceptions.ServiceNotAllowedException;
import com.crs4.sem.model.Document;
import com.crs4.sem.model.Metadata;
import com.crs4.sem.model.NewDocument;
import com.crs4.sem.model.NewSearchResult;
import com.crs4.sem.model.PairStringInteger;
import com.crs4.sem.model.SearchResult;
import com.crs4.sem.model.StatusSingleton;
import com.crs4.sem.model.Term;
import com.crs4.sem.neo4j.exceptions.CategoryNotFoundInTaxonomyException;
import com.crs4.sem.neo4j.exceptions.TaxonomyNotFoundException;
import com.crs4.sem.neo4j.service.TaxonomyService;
import com.crs4.sem.producers.AnalyzerType;
import com.crs4.sem.producers.Analyzers;
import com.crs4.sem.producers.DocumentProducerType;
import com.crs4.sem.producers.ServiceType;
import com.crs4.sem.rest.exceptions.ForbiddenStatusException;
import com.crs4.sem.rest.exceptions.InternalServerException;
import com.crs4.sem.rest.exceptions.MalformedQueryException;
import com.crs4.sem.service.DocumentService;
import com.crs4.sem.service.LuceneService;
import com.crs4.sem.service.NERService;
import com.crs4.sem.service.NewDocumentService;
import com.crs4.sem.service.ShadoService;
import com.crs4.sem.utils.Resolution;
import com.ibm.icu.util.Calendar;
import com.mfl.sem.classifier.text.TextClassifier;
import com.mfl.sem.model.ScoredItem;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.Data;

//@Stateless
//@ApplicationScoped
@Path("/documents")
@Api(value = "Documents", description = "Documents")
@Data
public class DocumentRestResources {

	@Inject
	@DocumentProducerType(ServiceType.DOCUMENT)
	private NewDocumentService documentService;

	@Inject
	TextClassifier textClassifier;
	@Inject
	private Logger log;

	@Inject
	@AnalyzerType(Analyzers.ITALIAN)
	Analyzer analyzer;

	@Inject
	private NERService nerservice;

	@Inject
	private SemEngineConfig semEngineConfig;

	@Inject
	private TaxonomyService taxonomyService;

	@Inject
	@DocumentProducerType(ServiceType.LUCENESERVICE)
	private LuceneService luceneService;
	
	@Inject
	@DocumentProducerType(ServiceType.SHADO)
	private ShadoService shadoService;
	
	@Inject
	private StatusSingleton status;
	
	public static double th0=0.9f;
	public static float alpha=1f;

	@GET
	@Path("/searchText")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "search text", notes = "Method for searching text", response = NewDocument.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Got it"),
			@ApiResponse(code = 500, message = "Server is down!") })
	public NewSearchResult  searchText(@QueryParam("text") @DefaultValue("") String text,
			@QueryParam("entities") @DefaultValue("false") Boolean entities,
			@QueryParam("start") @DefaultValue("0") Integer start,
			@QueryParam("maxresults") @DefaultValue("100") Integer maxresults, @QueryParam("from") String from,
			@QueryParam("to") String to,
			@QueryParam("threshold") @DefaultValue("2.9") double threshold) throws Exception {

		log.info("search text" + text);
		Date from_date = null;
		Date to_date = null;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		if (from != null)
			from_date = df.parse(from);
		if (to != null)
			to_date = df.parse(to);
		NewSearchResult searchResult = this.luceneService.parseSearch(text, "", from_date, to_date, start, maxresults, false, analyzer, false);
		List<NewDocument> duplicated = documentService.removeDuplicated(searchResult.getDocuments(),threshold);
		searchResult.setDuplicated(duplicated);
		return searchResult;
	}

	@GET
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Advanced search text", notes = "Advanced method for searching text ")
	
public NewSearchResult search(@QueryParam("text") @DefaultValue("") String text,
			@QueryParam("query") @DefaultValue("") String query, @QueryParam("start") @DefaultValue("0") Integer start,
			@QueryParam("maxresults") @DefaultValue("10") Integer maxresults, @QueryParam("from") String from,
			@QueryParam("to") String to, @QueryParam("score") @DefaultValue("false") boolean score,@QueryParam("links") @DefaultValue("true") Boolean links,
			@QueryParam("threshold") @DefaultValue("2.9") double threshold) throws Exception {
		 
		log.info("search text" + text + " with query" + query);
		Date from_date = null;
		Date to_date = null;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		if (from != null)
			from_date = df.parse(from);
		if (to != null)
			to_date = df.parse(to);
		NewSearchResult searchResult = this.documentService.parseSearch(text, query, from_date, to_date, start, maxresults,
				score, analyzer,links);
		List<NewDocument> duplicated = documentService.removeDuplicated(searchResult.getDocuments(),threshold);
		searchResult.setDuplicated(duplicated);
		return searchResult;
	}

	@GET
	@Path("/duplicated")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Search duplicated", notes = "rest for searching duplicated"
			)

	public List<NewDocument> duplicated(
			@QueryParam("tilte") @DefaultValue("") String title,
			@QueryParam("description") @DefaultValue("") String description,
			@QueryParam("url")  String url,
		 @QueryParam("start") @DefaultValue("0") Integer start,
			@QueryParam("maxresults") @DefaultValue("10") Integer maxresults,
			@QueryParam("threshold") @DefaultValue("2.9") float threshold) throws MalformedQueryException {
		return this.documentService.searchEquals(title, description, url, start, maxresults,threshold);
	}
	@GET
	@Path("/advancedsearch")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Advanced search text", notes = "Advanced method for searching text."
			+ " Setting text for free semantic search. Setting field to make constraints. "
			+ "Date format yyyy-MM-dd. Parameter score to force ranking with custom score."
			+ " Semantics parameter activate query espansion, "
			+ "the text is classificated by default taxonomy and query is expanded using this rule: query +cat*p(q/cat)^alpha ")

	public NewSearchResult advancedsearch(@QueryParam("text") @DefaultValue("") String text,
			@QueryParam("query") @DefaultValue("") String query_,
			@QueryParam("authors") @DefaultValue("") String authors,
			@QueryParam("source_id") @DefaultValue("") String source,
			@QueryParam("categories") @DefaultValue("") String categories,
			@QueryParam("type") @DefaultValue("") String type, @QueryParam("start") @DefaultValue("0") Integer start,
			@QueryParam("maxresults") @DefaultValue("10") Integer maxresults,
			@ApiParam(value = "Date from") @QueryParam("from") String from,
			@ApiParam(value = "Date to") @QueryParam("to") String to,
			@QueryParam("score") @DefaultValue("false") boolean score,
			@QueryParam("histograms") @DefaultValue("false") boolean histograms,
			@QueryParam("samplesize") @DefaultValue("100") Integer samplesize,
			@QueryParam("detect") @DefaultValue("false") boolean detect,
			@QueryParam("resolution") @DefaultValue("DAY") Resolution resolution,
			@QueryParam("links") @DefaultValue("false") boolean links,
			@QueryParam("threshold") @DefaultValue("2.9") double threshold,
			@QueryParam("semantics") @DefaultValue("false") boolean semantics,
			@QueryParam("classify") @DefaultValue("false") boolean classify,
			@QueryParam("entities") @DefaultValue("false") boolean entities
			) throws Exception {

		long times = System.currentTimeMillis();
		Date from_date = null;
		Date to_date = null;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		if (from != null)
			from_date = df.parse(from);
		if (to != null)
			to_date = df.parse(to);
		List<NewDocument> docs;
		String query = query_;
		if (!authors.trim().isEmpty())
			query += " +(authors:" + authors + ")";
		if (!type.trim().isEmpty())
			query += " +(type:" + type + ")";
		if (!source.trim().isEmpty())
			query += " +(source_id:" + source + ")";
		if (!categories.trim().isEmpty())
			query += " +(categories:" + categories + ")";
		log.info("search text" + text +(query==null?"":" query "+ query)+(from==null?"":" from "+from)+ (to==null?"":" to "+to)+" start "+ start+" maxresults "+ maxresults + ""+ ((histograms)? "with statistics samplesize:"+samplesize :""));
		if(semantics) {
			List<ScoredItem> catlist = this.textClassifier.classify(NewDocument.builder().title(text).build());
		    if(!catlist.isEmpty()&&catlist.get(0).getScore()>th0)
		    	log.info(" categories "+ catlist.get(0));
		    	query+= "  OR (categories: "+catlist.get(0).getLabel()+ ")";
		}
		NewSearchResult searchResult = this.documentService.parseSearch(text, query, from_date, to_date, start, maxresults,
				score, analyzer,links);
		List<NewDocument> duplicated = documentService.removeDuplicated(searchResult.getDocuments(),threshold);
		searchResult.setDuplicated(duplicated);
       samplesize=samplesize>1000?1000:samplesize;
		if (histograms) {
			if (start == 0 && maxresults >= samplesize) {
				if (detect) {
					Analyzer analyzer = new ShingleAnalyzerWrapper(2, 3);
					Set<String> set = this.taxonomyService.getAllKeywords("root", false);
					this.detectKeywords(searchResult.getDocuments(), set, analyzer);
					this.classifyDocuments(searchResult.getDocuments(), textClassifier);
				}
				searchResult = this.histograms(searchResult, samplesize, resolution);
			}
			else {
				
				NewSearchResult temp = this.documentService.parseSearch(text, query, from_date, to_date, 0, samplesize,
						score, analyzer,links);
				 duplicated = documentService.removeDuplicated(temp.getDocuments(),threshold);
				temp.setDuplicated(duplicated);

				if (detect) {
					Analyzer analyzer = new ShingleAnalyzerWrapper(2, 3);
					Set<String> set = this.taxonomyService.getAllKeywords("root", false);
					this.detectKeywords(temp.getDocuments(), set, analyzer);
					this.classifyDocuments(temp.getDocuments(), textClassifier);
				}
				temp = this.histograms(temp, samplesize, resolution);
				searchResult.setCategories(temp.getCategories());
				searchResult.setKeywords(temp.getKeywords());
				searchResult.setDates(temp.getDates());
				searchResult.setAuthors(temp.getAuthors());
				searchResult.setTypes(temp.getTypes());
				
				
			}

		}

		searchResult= this.addMetadataFromShado(shadoService,searchResult);
		if(classify)
			this.classifyDocuments(searchResult.getDocuments(), textClassifier);
		if(entities)
			this.detectEntities(searchResult.getDocuments(),this.nerservice);
		times=System.currentTimeMillis()-times;
		System.out.println("advanced search time elapsed " +times);
		return searchResult;
	}

	public void detectEntities(List<NewDocument> documents, NERService nerservice) {
		
		for(NewDocument doc:documents) {
			Set<String> result= new HashSet<String>();
			List<Term> taggedterms=new ArrayList<Term>();
			try {
				if(doc.getTitle()!=null&&!doc.getTitle().isEmpty())
						taggedterms = nerservice.tagSentences(doc.getTitle());
				List<Term> entities = nerservice.listOfEntities(taggedterms);
				for(Term e:entities)
					result.add(e.content()+":"+e.tag());
				if(doc.getDescription()!=null&&!doc.getDescription().isEmpty())
					taggedterms = nerservice.tagSentences(doc.getDescription());
		         entities = nerservice.listOfEntities(taggedterms);
			for(Term e:entities)
				result.add(e.content()+":"+e.tag());
			} catch (IOException e) {
			  log.info(""+e);
			}
			String [] array= new String[result.size()];
			doc.setEntities(result.toArray(array));
		}		
	}

	private NewSearchResult addMetadataFromShado(ShadoService shadoService, NewSearchResult searchResult) {
		//List<NewDocument> pdocuments= new ArrayList<NewDocument>();
		for(NewDocument doc:searchResult.getDocuments()) {
			
			
			addMetadataFromShado(shadoService, doc);
	
	
		}
	
		return searchResult;
	}

	public  void addMetadataFromShado(ShadoService shadoService, NewDocument doc) {
		if(shadoService.getShado(doc.getInternal_id())!=null) {
			if(doc.getLinks()!=null)
		  doc.setLinks(shadoService.getPage(doc.getLinks()));
			 if(doc.getMovies()!=null)
		  doc.setMovies(shadoService.getMetadata(doc.getMovies()));
			 if(doc.getAttachments()!=null)
		  doc.setAttachments(shadoService.getMetadata(doc.getAttachments()));
			 if(doc.getPodcasts()!=null)
		  doc.setPodcasts(shadoService.getMetadata(doc.getPodcasts()));
			 if(doc.getGallery()!=null)
		  doc.setGallery(shadoService.getMetadata(doc.getGallery()));
		 }
	}

	private List<Metadata> getMetadata(String[] links) {
		List<Metadata> metadatas= new ArrayList<Metadata>();
		
		for(String link:links) {
			{ Metadata current = Metadata.builder().url(link).build();		
			 metadatas.add(current);
			 }
			
				 
		}
		return metadatas;
	}

	public void classifyDocuments(List<NewDocument> documents, TextClassifier textClassifier) {
		for (NewDocument doc : documents) {
			List<ScoredItem> result;
			try {
				if(!doc.getTrainable())
				classifyDocument(textClassifier, doc);
			} catch (IOException e) {
				log.info(""+e);
			}
		}

	}

	public void classifyDocument(TextClassifier textClassifier, NewDocument doc) throws IOException {
		List<ScoredItem> result;
		result = textClassifier.classify(doc);

		String categories[] = null;
		if (result.size() >= 2) {
			categories = new String[2];
			categories[0] = result.get(0).getLabel();
			categories[1] = result.get(1).getLabel();
		} else if (result.size() == 1) {
			categories = new String[1];
			categories[0] = result.get(0).getLabel();
		}

		if (categories != null)
			doc.setCategories(categories);
	}

	private void detectKeywords(List<NewDocument> documents, Set<String> set, Analyzer analyzer) {
		for (NewDocument doc : documents) {
			try {
				String[] keys = DocumentService.keywordsDetect(doc.text(), set, analyzer);
				doc.setKeywords(keys);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private NewSearchResult histograms(NewSearchResult searchResult, Integer samplesize, Resolution resolution) {
		List<NewDocument> docs = searchResult.getDocuments();
		Map<String, PairStringInteger> keywordsmap = new HashMap<String, PairStringInteger>();
		Map<String, PairStringInteger> categoriesmap = new HashMap<String, PairStringInteger>();
		Map<String, PairStringInteger> authorsmap = new HashMap<String, PairStringInteger>();
		Map<String, PairStringInteger> datesmap = new HashMap<String, PairStringInteger>();
		Map<String, PairStringInteger> typesmap = new HashMap<String, PairStringInteger>();
		for (NewDocument doc : docs) {
			calculateFrequency(keywordsmap, doc.getKeywords());
			calculateFrequency(categoriesmap,doc.getCategories());
			if(doc.getAuthors()!=null)
				calculateFrequency(authorsmap,doc.getAuthors().split(","));
			if(doc.getType()!=null)
			calculateFrequency(typesmap,doc.getType().split(","));
			//LocalDate localDate = doc.getPublishDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			//String year = localDate.getYear()+"";
			
			String [] tt=new String[1];
			tt[0]=resolution.format(doc.getPublishDate());
			calculateFrequency(datesmap,tt);
		}
		List<PairStringInteger> cats = new ArrayList<PairStringInteger>(categoriesmap.values());
		List<PairStringInteger> keys = new ArrayList<PairStringInteger>(keywordsmap.values());
		List<PairStringInteger> auts = new ArrayList<PairStringInteger>(authorsmap.values());
		List<PairStringInteger> dats = new ArrayList<PairStringInteger>(datesmap.values());
		List<PairStringInteger> types = new ArrayList<PairStringInteger>(typesmap.values());
		Collections.sort(cats);
		Collections.sort(keys);
		Collections.sort(auts);
		Collections.sort(types);
		Collections.sort(dats, new Comparator<PairStringInteger>() {
		    @Override
		    public int compare(PairStringInteger left, PairStringInteger right) {
		        return left.getKey().compareTo(right.getKey()); // use your logic
		    }
		});
		searchResult.setCategories(cats);
		searchResult.setKeywords(keys);
		searchResult.setDates(dats);
		searchResult.setAuthors(auts);
		searchResult.setTypes(types);
		return searchResult;
	}

	private void calculateFrequency(Map<String, PairStringInteger> mapaux, String [] words) {
		
		if( words != null)
			for (String key : words) {
				PairStringInteger freq = mapaux.get(key);
				if (freq != null)
					freq.setF(freq.getF() + 1);
				else
					mapaux.put(key, PairStringInteger.builder().key(key).f(1).build());
			}
	}

	@POST
	@Path("/add")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "add document", notes = "Add a single document. Update/set  timestamp to current time.")
 public Response  addDocument(NewDocument doc) throws Exception {

		if (status.isAllowadd()) {
		//	addingdocs=false;
			log.info("add document" + doc);
			doc.setTimestamp(Calendar.getInstance().getTime());
			doc.assignIdentifiers();
	        
			 doc.setId(NewDocument.setHashID(doc.getInternal_id().getBytes()));
			 this.documentService.checkSingleDocument(doc);
			this.documentService.saveOrUpdateDocument(doc);
			log.info("added document");
			return Response.ok("added document").build();
		} else
			return Response.status(Status.FORBIDDEN).build();
	}

	@POST
	@Path("/addAll")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "addAll", notes = "Add a list of documents. Update/set  timestamp to current time.")
 public Response addDocuments(List<NewDocument> documents,@QueryParam("keywords") @DefaultValue("true") Boolean keywords,@QueryParam("classify") @DefaultValue("true") Boolean classify) throws Exception {

		if (status.isAllowadd()) {
		//	addingdocs=false;
			log.info("documents/addAll  starting");
			for(NewDocument doc:documents)
				doc.setTimestamp(Calendar.getInstance().getTime());
			int sz = documents.size();
			//documents=this.documentService.cleanReplicas(documents);
		
				//log.info(" documents/addAll found " + (sz-documents.size()) + "replicas from "+ sz + " posted ");
			
			if(keywords) {
				Analyzer analyzer = new ShingleAnalyzerWrapper(2, 3);
				Set<String> keyset = this.taxonomyService.getAllKeywords("root", true);
				for(NewDocument doc:documents) {
				String [] keyws = DocumentService.keywordsDetect(doc.text(), keyset, analyzer);
				doc.setKeywords(keyws);
				}
			}
			
			if(classify) {
				for(NewDocument doc:documents) {
				List<ScoredItem> result = textClassifier.classify(doc);

				String[] categories = DocumentService.firstTwoCategories(result);
                doc.setCategories(categories);
				
				}
			}
			this.documentService.assignIdentifiers(documents);
			this.documentService.checkDocuments(documents);
			int num=this.documentService.saveOrUpdateAll(documents);
			log.info("documents/addAll added documents");
			//addingdocs=true;
			
			return Response.ok("uploaded "+num+" documents").build();
		} else
			return Response.status(Status.FORBIDDEN).build();
	}

	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Upload documents", notes = "Upload stream of documents")
public Response uploadDocuments(@FormDataParam("file") InputStream inputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail)  {
		log.info("adding " + fileDetail.getFileName()+ " documents");
		Integer num;
		try {
			num = documentService.addDocuments(inputStream);
		} catch (Exception e) {
			throw new InternalServerException(e+"");
		}
		return Response.ok("uploaded "+num+" documents" ).build();

	}

	@GET
	@Path("/internal/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "get document", notes = "get document by its internal id")
	public NewDocument getById(@PathParam("id") String id,@QueryParam("links") @DefaultValue("true") Boolean links,@QueryParam("classify") @DefaultValue("false") Boolean classify) throws IOException {
		NewDocument doc = this.documentService.getById(id,links);
		if(classify)
			this.classifyDocument( textClassifier,doc);
		log.info("get document " + id);

		return doc;
	}
	
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "get document", notes = "get document by its id")
	public NewDocument get(@PathParam("id") Long id,@QueryParam("shado") @DefaultValue("true") Boolean shado,
			@QueryParam("links") @DefaultValue("true") Boolean links, @QueryParam("classify") @DefaultValue("false") Boolean classify) throws IOException {
		NewDocument doc = this.documentService.getNaturald( id,links);
		if(shado)
			this.addMetadataFromShado(shadoService, doc);
		if(classify)
			this.classifyDocument( textClassifier,doc);
		log.info("get document " + id);
		return doc;
	}

	@GET
	@Path("/dump")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "get document", notes = "Get paginated documents. Date format yyyy-MM-dd HH:mm:ss ")
public NewSearchResult dump(@QueryParam("start") @DefaultValue("0") Integer start,
			@QueryParam("maxresults") @DefaultValue("10") Integer maxresults,
			@ApiParam(value = "Date from") @QueryParam("from") String from,
			@ApiParam(value = "Date to") @QueryParam("to") String to,
			@QueryParam("links") @DefaultValue("true") boolean links) throws ParseException {
		if(!status.isAllowadd()) throw new ForbiddenStatusException("Dump service is disabled");
		Date from_date = null;
		Date to_date = null;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (from != null)
			from_date = df.parse(from);
		if (to != null)
			to_date = df.parse(to);
		NewSearchResult docs = this.documentService.dump(start, maxresults, from_date, to_date,links);
		log.info("dump: get all documents from date:" + from + "to date " + to);
      //  SearchResult result= this.convertToSearchResult(docs);
		
		return docs;
	}

	

	private SearchResult convertToSearchResult(NewSearchResult newsearchresult) {
		 List<NewDocument> documents = newsearchresult.getDocuments();
		 List<Document> olddocs= new ArrayList<Document>();
		 for(NewDocument doc:documents) {
		    Document olddoc=Document.toDocument(doc);
		    olddocs.add(olddoc);
		 }
		 return SearchResult.builder().documents(olddocs).totaldocs(newsearchresult.getTotaldocs()).build();
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	//@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "delete document from documents database", notes = "Delete document from documents database ")
	public Response deleteDocument(@ApiParam(value = "document id" )@PathParam("id") String id) {
		NewDocument doc=this.documentService.deleteDocument(id);
		if(doc==null) return Response.status(Status.NOT_FOUND).build();
		return Response.ok().build();
	}

//	@GET
//	@Path("/download")
//	@Produces(MediaType.APPLICATION_OCTET_STREAM)
//	public Response downloadFileWithGet(@QueryParam("file") String file) {
//
//		String path = System.getProperty("user.home") + File.separator + "uploads";
//		File fileDownload = new File(path + File.separator + file);
//		ResponseBuilder response = Response.ok((Object) file);
//		response.header("Content-Disposition", "attachment;filename=" + file);
//		return response.build();
//	}

	@GET
	@Path("/rebuild")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Rebuild the whole index", notes = "Rebuild the whole index. Essential when change the document structure")
	  public Response rebuild() throws InterruptedException
			 {
		if (!status.isAllowadd()) {
			//addingdocs=false;
		log.info("rebuildind index structure, this operation can take many time ");
		this.documentService.rebuildIndex();
		return Response.ok("rebuilt index").build();
		//addingdocs=true;
		}
		return Response.status(Status.FORBIDDEN).build();
//		if (keywords) {
//			Set<String> set = this.taxonomyService.getAllKeywords("root", true);
//
//			this.documentService.detectKeywords(set);
//		}

		
	}

	@GET
	@Path("/detect/keywords/{taxomyname}")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Detect keywords from the taxonomy in the whole documents database", notes = "Detect keywords from the taxonomy in the whole documents database. Set taxonomy name")
	public Response detectKeywords(@PathParam("taxonomyname") @DefaultValue("root") String taxonomyname)
			throws TaxonomyNotFoundException, CategoryNotFoundInTaxonomyException {

		log.info("Detect keywords from taxonomy : " + taxonomyname);
		Set<String> set = this.taxonomyService.getAllKeywords("root", false);
		Long value = this.documentService.detectKeywords(set);

		return Response.ok("detect keywords in " + value + "documents").build();
	}


	@GET
	@Path("/size")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Get size of documents", notes = "get the size of documents")
	public Integer size() throws IOException {

		Integer size = this.documentService.size();
		log.info("getting size " + size);
		return size;

	}
	

	@GET
	@Path("/indexsize")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Get size of indexed documents", notes = "get the size of indexed documents")
	public Integer indexsize()  {

		Integer size = this.documentService.indexsize();
		log.info("getting index size " + size);
		return size;

	}
	@GET
	@Path("/disable")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Disable   input services", notes = "disable input service")
	public Boolean disable(@QueryParam("adding") @DefaultValue("true") Boolean adding)  {

	 this.status.setAllowadd(adding);
		log.info("disabled adding documents ");
		return adding;

	}
	
	@GET
	@Path("/status")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Get status", notes = " get status of services")
	public Boolean status()  {

	 return this.status.isAllowadd();
		

	}
	@GET
	@Path("/optimize")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Optimize lucene index", notes = "optimize lucene index")
	public Response optimize() throws IOException {

	 if(!status.isAllowadd()) {
		 this.documentService.optimizeIndex();
		log.info(" optimized index ");
		
		return Response.ok("optimized index").build();
	}
	 return  Response.status(Status.FORBIDDEN).build();
	}
}
