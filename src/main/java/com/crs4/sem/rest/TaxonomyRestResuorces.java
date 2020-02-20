
package com.crs4.sem.rest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.neo4j.graphdb.Node;

import com.crs4.sem.model.Documentable;
import com.crs4.sem.model.NewDocument;
import com.crs4.sem.neo4j.exceptions.CategoryNotFoundException;
import com.crs4.sem.neo4j.exceptions.CategoryNotFoundInTaxonomyException;
import com.crs4.sem.neo4j.exceptions.TaxonomyNotFoundException;
import com.crs4.sem.neo4j.model.CategoryNode;
import com.crs4.sem.neo4j.service.TaxonomyCSVReader;
import com.crs4.sem.neo4j.service.TaxonomyService;
import com.crs4.sem.producers.DocumentProducerType;
import com.crs4.sem.producers.ServiceType;
import com.crs4.sem.rest.exceptions.CategoryExceedException;
import com.crs4.sem.service.NewDocumentService;
import com.crs4.sem.utils.DocumentsUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.Data;

@Data
@Stateless
//@ApplicationScoped
@Path("/taxonomy")
@Api(value = "Taxonomy", description = "Resources api for building and organizing taxonomy classifiers")
public class TaxonomyRestResuorces {

	@Inject
	private TaxonomyService taxonomyService;

	@Inject
	private Logger log;
	
	
	@Inject
	@DocumentProducerType(ServiceType.DOCUMENT)
	private NewDocumentService documentService;


	@POST
	@Path("/{name}/category/add/{parent}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Add partent to category", notes = "add category to partent")
	public String addCategory(@ApiParam(value = "taxonomy name" ) @DefaultValue("root") @PathParam("name") String name,@PathParam("parent") String parent_name, @ApiParam(value = "category id" ) String category_name)
			throws IOException, InterruptedException, CategoryNotFoundException {

		log.info("add category" + category_name);
		Node category = null;
		Node parent = this.getTaxonomyService().searchCategory(parent_name);
		if (parent == null)
			throw new CategoryNotFoundException();
		if (this.getTaxonomyService().searchCategory(category_name) == null)
			category = this.getTaxonomyService().createCategory(category_name);

		this.getTaxonomyService().addToParent(parent, category);
		return "ok";
	}

	@DELETE
	@Path("/{name}/category/{id}")
	// @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "delete category from parent", notes = "delete category from partent")
	public Response deleteCategory(@ApiParam(value = "taxonomy name" )   @PathParam("name") @DefaultValue("root") String name,@ApiParam(value = "parent id" ) @PathParam("id") String id, @ApiParam(value = "category id" )String category) {
		log.info("delete category " + id + " from taxonomy "+ name);
		return Response.status(200).build();
	}

	@GET
	@Path("/{name}/category/branch/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	// @Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get a brach starting from a category ", notes = "Return a branch from a given category ")
	public CategoryNode getBranch(@ApiParam(value = "taxonomy name" )  @PathParam("name") @DefaultValue("root") String name, @DefaultValue("root") @ApiParam(value = "category id" ) @PathParam("id") String id) {
		log.info("get branch from taxonomy "+name +"starting from category"+ id);
		return this.taxonomyService.getBranch(name, id);
	}

	@DELETE
	@Path("/{name}/keyword/{id}")
	// @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Delete keyword from category", notes = "Delete a keyword from category ")
	public Response delete(@ApiParam(value = "taxonomy name" ) @PathParam("name") @DefaultValue("root") String name,@ApiParam(value = "category id" ) @PathParam("id") String id) {
		log.info("delete keyword "+id+" from taxonomy" + name);
		return null;
	}

	@PUT
	@Path("/{name}/keyword/{id}")
	// @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "put keyword to category", notes = "Put a keyword to category id")
	public Response putKeyword(@ApiParam(value = "taxonomy name" )  @PathParam("name") @DefaultValue("root") String name,@ApiParam(value = "category id" ) @PathParam("id") String id, @ApiParam(value = "keyword" ) String keyword) throws CategoryNotFoundException {
		log.info("put keyword to category"+id+"  to taxonomy" + name);
		
		this.taxonomyService.addKeyword(id, keyword);
		return Response.ok().build() ;
	}
	@GET
	@Path("/{name}/keywords/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	//@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "get keywords from given category id ", notes = "Get keywords from given category id ")
	public String [] getKeyword(@ApiParam(value = "taxonomy name" )  @PathParam("name") @DefaultValue("root") String name,@ApiParam(value = "category id" ) @PathParam("id") String id ) throws CategoryNotFoundInTaxonomyException {
		log.info("getting keywords from category:"+id+"from taxonomy"+name);
		return this.taxonomyService.getKetwords(name, id);
	}

//	@PUT
//	@Path("/{name}/document/{id}")
//	// @Produces(MediaType.APPLICATION_JSON)
//	@Consumes(MediaType.TEXT_PLAIN)
//	@Produces(MediaType.TEXT_PLAIN)
//	@ApiOperation(value = "put document to category ", notes = "Put document to category ")
//	public Response putDocument(@ApiParam(value = "taxonomy name" ) @PathParam("name") @DefaultValue("root") String name,@ApiParam(value = "category id" ) @PathParam("id") String category_id,@ApiParam(value = "exclusive" ) @QueryParam("exclusive") @DefaultValue("false") Boolean exclusive  , String document_id) throws CategoryNotFoundException {
//		log.info("put document "+document_id+" to taxonomy:"+name+"from category"+category_id);
//		NewDocument doc = this.documentService.getById(document_id, true);
//		String categories[]=null;
//		if(doc.getTrainable()==false)
//			categories= new String[1];
//		else 
//			categories=doc.getCategories();
//		if(categories.length==2) throw new CategoryExceedException("exceed two categories");
//		if(categories.length==1)
//		{
//			String [] newcategories= new String[2];
//			newcategories[0]=categories[0];
//			newcategories[1]=category_id;
//			doc.setCategories(newcategories);
//		}
//		else {
//			String [] newcategories= new String[1];
//			newcategories[0]=category_id;
//			doc.setCategories(newcategories);
//		}
//		
//		
//		doc.setTrainable(true);
//		this.documentService.updateDocument(doc);
//		this.taxonomyService.addDocument(category_id, document_id,exclusive);
//		
//		return Response.ok("added document to category "+category_id).build();
//	}

	@PUT
	@Path("/{name}/document/{id}")
	// @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "put categories to document id", notes = "Put categories to document id ")
	public Response putCategoriesToDocument(@ApiParam(value = "taxonomy name" ) @PathParam("name") @DefaultValue("root") String name,@ApiParam(value = "document id" ) @PathParam("id")  String document_id, String [] categories) 
      throws CategoryNotFoundException {
		log.info("put categories  to document id");
		NewDocument doc = this.documentService.getById(document_id, true);
	    String [] oldvalues=doc.getCategories();
		if(categories.length>2) throw new CategoryExceedException("exceed two categories");
		doc.setCategories(categories);
		
		
		
		doc.setTrainable(true);
		this.documentService.updateDocument(doc);
		for(String cat:oldvalues)
			this.taxonomyService.deleteDocument(cat, document_id);
		for(String cat:categories)
		  this.taxonomyService.addDocument(cat, document_id,false);
		
		return Response.ok("added categories "+Arrays.toString(categories) + " to document "+ document_id).build();
	}
	@GET
	@Path("/{name}/documents/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	//@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "get documents from given category id ", notes = "Get documents from given category id ")
	public String [] getDocument(@ApiParam(value = "taxonomy name" )  @PathParam("name") @DefaultValue("root") String name,@ApiParam(value = "category id" ) @PathParam("id") String id ) throws CategoryNotFoundInTaxonomyException {
		log.info("getting documents from category:"+id+"from taxonomy"+name);
		return this.taxonomyService.getDocuments(name, id);
	}
	
	@GET
	@Path("/{name}/documents")
	@Produces(MediaType.APPLICATION_JSON)
	//@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "get all trainable documents from taxonomy ", notes = "Get documents from taxonomy ")
	public Set<String> getDocument(@ApiParam(value = "taxonomy name" )  @PathParam("name") @DefaultValue("root") String name) throws TaxonomyNotFoundException, CategoryNotFoundInTaxonomyException {
		log.info("getting documents from taxonomy:"+name);
		return this.taxonomyService.getAllDocuments(name);
	}
	
	@DELETE
	@Path("/{name}/document")
    @Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "delete document", notes = "Delete document from taxonomy")
	public Response deleteDocument(@ApiParam(value = "taxonomy name" ) @PathParam("name")  @DefaultValue("root")String name,@QueryParam("document_id")  String document_id) {
		log.info("delete document "+document_id+" from taxonomy "+ name);
		NewDocument doc = this.documentService.getById(document_id, true);
	
		doc.setCategories(null);
		doc.setTrainable(false);
		this.documentService.saveOrUpdateDocument(doc);
		this.taxonomyService.deleteDocument(document_id);
		return Response.ok("deleted").build();
	}
	
	@DELETE
	@Path("/{name}/document/{id}")
    @Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "delete document", notes = "Delete document from category")
	public Response deleteDocumentFromCategory(@ApiParam(value = "taxonomy name" ) @PathParam("name")  @DefaultValue("root")String name,@ApiParam(value = "category id" ) @PathParam("id") String id, @QueryParam("document_id")  String document_id) {
		log.info("delete document "+document_id+" from taxonomy "+ name + "category"+id);
		NewDocument doc = this.documentService.getById(document_id, true);	
		String [] categories=doc.getCategories();
	    categories=this.removeCategory(categories,id);
	    doc.setCategories(categories);
		if(categories.length==0) 
			this.taxonomyService.deleteDocument(document_id);
		
		else 
			this.taxonomyService.deleteDocument(id,document_id);
		this.documentService.saveOrUpdateDocument(doc);
		
		return Response.ok("deleted").build();
	}
	
	private String[] removeCategory(String categories[], String id) {
		List<String> strings= new ArrayList<String>();
		for(String cat:categories)
			if(!cat.equals(id))
				strings.add(cat);
		return strings.toArray(new String[strings.size()]);
	}

	@DELETE
	@Path("/{name}/documents")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "delete all document", notes = "Delete all documents from taxonomy")
	public Response deleteDocuments(@ApiParam(value = "taxonomy name" ) @PathParam("name")  @DefaultValue("root")String name) throws TaxonomyNotFoundException, CategoryNotFoundInTaxonomyException {
		log.info("delete all documents ");
	
		Set<String> alls=this.taxonomyService.getAllDocuments(name);
		for( String id_doc:alls) {
			NewDocument doc = this.documentService.getById(id_doc, true);
			
			doc.setCategories(null);
			doc.setTrainable(false);
			this.documentService.updateDocument(doc);
			this.taxonomyService.deleteDocument(id_doc);
		}
		return Response.ok("deleted "+alls.size()+"  documents").build();
	}

	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "upload taxonomy", notes = "Upload a complete csv taxonomy")
	public Response uploadTaxo(@FormDataParam("file") InputStream inputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) throws IOException {
		log.info("added " + fileDetail.getFileName());
		TaxonomyCSVReader.readIStream(inputStream, taxonomyService);
		return Response.ok().build();

	}

	@POST
	@Path("/keywords/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "upload  keyword set", notes = "Upload keywords to taxonomy ")
	public Response uploadKeywords(@FormDataParam("file") InputStream inputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) throws IOException {
		log.info("added " + fileDetail.getFileName());
		TaxonomyCSVReader.readKeywordsStream(inputStream, taxonomyService);
		return Response.ok().build();
	}
	

	@GET
	@Path("/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "get  Taxonomy ", notes = "Get Taxonomy by name ")
	public String[] getTaxonomy(@DefaultValue("root") @PathParam("name") String name) throws TaxonomyNotFoundException  {
		log.info("get taxonomy "+name);
		Node root=taxonomyService.searchCategory(name);
		if(root==null) throw new com.crs4.sem.neo4j.exceptions.TaxonomyNotFoundException();
	    String[] labels = taxonomyService.branchLabels(root, true);
		return labels;
	}
	
	@GET
	@Path("/{name}/keywords")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "get  keywords Taxonomy ", notes = "Get Taxonomy Keywords by name ")
	public Set<String> getTaxonomyKeywords(@DefaultValue("root") @PathParam("name") String name) throws TaxonomyNotFoundException, CategoryNotFoundInTaxonomyException  {
		log.info("get taxonomy "+name);
		Set<String> set = taxonomyService.getAllKeywords(name, false);
		return set;
	}
	
	@DELETE
	@Path("/{name}")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "delete document", notes = "Delete document from category id")
	public Response deleteTaxonomy(@PathParam("name") @DefaultValue("root") String name) throws com.crs4.sem.neo4j.exceptions.TaxonomyNotFoundException {
		log.info("delete taxonomy:"+name);
		this.getTaxonomyService().deleteTaxonomy( name);
		return Response.ok("deleted taxonomy "+name).build();
	}
	@POST
	@Path("/triplet/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "upload taxonomy in triple format", notes = "Upload a complete csv taxonomy")
	public Response uploadTripletTaxo(@FormDataParam("file") InputStream inputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) throws IOException {
		log.info("upload taxonomy  " + fileDetail.getFileName() +" with triplet format");
		TaxonomyCSVReader.readTriple(inputStream, taxonomyService);
		return Response.ok().build();

	}
	
	@GET
	@Path("/{name}/documents/download")
	@ApiOperation(value = "download taxonomy documents", notes = "download taxonomy documents")
	public Response dowloadDocuments(@PathParam("name") @DefaultValue("root") String taxoname) throws TaxonomyNotFoundException, CategoryNotFoundInTaxonomyException, JsonGenerationException, JsonMappingException, IOException {
		
		
		 List<NewDocument> list= DocumentsUtil.getAllTrainableDocument(taxonomyService, documentService, taxoname,true);
		 
		 StringWriter writer= new StringWriter();
		
		 ObjectMapper objectMapper = new ObjectMapper();
		 objectMapper.setSerializationInclusion(Include.NON_NULL);
	     objectMapper.writeValue(writer, list);
		 return Response
	                .ok(writer.toString(), MediaType.APPLICATION_OCTET_STREAM)
	                .header("content-disposition","attachment; filename = "+taxoname+"_docs.json")
	                .build();
	}
	
	@GET
	@Path("/{name}/documents/update")
	@ApiOperation(value = "update set of documents. Useful when update taxonomy and documents", notes = "update documents")
	public Response update(@PathParam("name") @DefaultValue("root") String taxoname) throws TaxonomyNotFoundException, CategoryNotFoundInTaxonomyException {
		
		
		 List<NewDocument> list= DocumentsUtil.getAllTrainableDocument(taxonomyService, documentService, taxoname,true);
//		 int size=list.size();
//		 int slice=size/100;
//		 int index=0;
//		 for( int i=0;i<size;i+=slice) {
//			 int  min=Math.min(size, i+slice);
//		      List<NewDocument> sub = list.subList(i, min);
//		 
//			 documentService.saveOrUpdateAll(sub);
//			 index=i;
//		 }
//		 if(index<size) {
//			 List<NewDocument> sub = list.subList(index, size);
			 
			 documentService.saveOrUpdateAll(list);
		// }
			 
		 return Response.ok("updated "+ list.size() + " documents").build();
	}
	
	@POST
	@Path("/{name}/documents/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "upload documents ", notes = "Upload documents")
	public Response uploadDocuments(@PathParam("name") @DefaultValue("root") String taxoname,@FormDataParam("file") InputStream inputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) throws JsonParseException, JsonMappingException, IOException, CategoryNotFoundException  {
		log.info("upload documents " + fileDetail.getFileName() );
		
			
		 ObjectMapper objectMapper = new ObjectMapper();
	     List<NewDocument> documents=objectMapper.readValue(inputStream, new TypeReference<List<NewDocument>>(){});
	    for(NewDocument doc:documents) {
	 	for(String cat:doc.getCategories()) {
	 		  if(cat!=null&&!cat.trim().isEmpty())
			  this.taxonomyService.addDocument(cat, doc.getInternal_id(),false);
	 	}
	 	     
	    }
	    this.documentService.assignIdentifiers(documents);
		this.documentService.checkDocuments(documents);
	    this.documentService.saveOrUpdateAll(documents);
		return Response.ok("uploaded "+ documents.size()+ " documents").build();

	}

}
