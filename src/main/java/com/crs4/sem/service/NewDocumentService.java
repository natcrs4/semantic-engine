package com.crs4.sem.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.IdentifierLoadAccess;
import org.hibernate.NaturalIdLoadAccess;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SimpleNaturalIdLoadAccess;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Projections;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.stat.Statistics;
import org.hibernate.service.ServiceRegistry;
import org.neo4j.cypher.internal.frontend.v2_3.perty.recipe.Pretty.doc;
import org.neo4j.graphalgo.impl.PageRankResult;

import com.crs4.sem.analysis.CommaAnalyzer;
import com.crs4.sem.lucene.similarity.EntityRankScoreQuery;
import com.crs4.sem.lucene.similarity.ScoreRankScoreQuery;
import com.crs4.sem.model.Author;
import com.crs4.sem.model.Document;
import com.crs4.sem.model.Documentable;
import com.crs4.sem.model.NewDocument;
import com.crs4.sem.model.NewMetadata;
import com.crs4.sem.model.NewSearchResult;
import com.crs4.sem.model.Page;
import com.crs4.sem.model.Shado;
import com.crs4.sem.model.lucene.EntityDocumentBuilder;
import com.crs4.sem.rest.serializer.DateSerializer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mfl.sem.classifier.text.TextClassifier;
import com.mfl.sem.model.ScoredItem;

import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor

public class NewDocumentService extends HibernateService{
	public static final Logger logger = Logger.getLogger(NewDocumentService.class);
	
	
	public NewDocumentService(String connection_url, String lucene_indexbase, String directory_provider) {

		Configuration configuration = new Configuration();
		configuration = configuration.addAnnotatedClass(NewDocument.class);
		configuration = configuration.addAnnotatedClass(NewMetadata.class);
		configuration = configuration.addAnnotatedClass(Page.class);
		configuration = configuration.configure();
		configuration.setProperty("hibernate.search.default.directory_provider", directory_provider);
		configuration.setProperty("hibernate.connection.url", connection_url);
		configuration.setProperty("hibernate.search.default.indexBase", lucene_indexbase);
		ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
				.applySettings(configuration.getProperties()).build();
		factory = configuration.buildSessionFactory(serviceRegistry);
	};

	public NewDocumentService(Configuration configuration) {
		super(configuration);
	};
	
	public void saveAll(List<NewDocument> documents)  {
		this.checkDocuments(documents);
		Session session = factory.openSession();
		Transaction tx = session.beginTransaction();
		try {
			for (NewDocument  docs : documents) 
			{if(docs.getId()==null) {
           	 docs.setId(NewDocument.setHashID(docs.getInternal_id().getBytes()));
            }
				session.save(docs);
			}
			

			tx.commit();
		} catch (RuntimeException e){
			if (tx != null)
				tx.rollback();
			throw e; // or display error message
		}
	
		finally {
			session.close();
		}

	}
	public void assignIdentifiers(List<NewDocument> docs) {
		for(NewDocument doc:docs) {
			doc.assignIdentifiers();
		}
	     
	}
	
	public void checkDocuments(List<NewDocument> docs) {
		HashMap<String, NewMetadata> map = new HashMap<String,NewMetadata>();
		HashMap<String, Page> pages = new HashMap<String,Page>();
		for(NewDocument doc:docs) {
			this.checkNewDocument(doc, map, pages);
		}
	     
	}
	public Set<NewMetadata> checkMetadata( Set<NewMetadata>mts, Map<String,NewMetadata> map) {
		Set<NewMetadata> result= new HashSet<NewMetadata>();
		for(NewMetadata mt:mts) {
			 NewMetadata aux = map.get(mt.getId());
			if(aux!=null) result.add(aux);
			else {
				map.put(mt.getId(), mt);
				result.add(mt);
			}
		}
			return result;
		
	}
	public void checkNewDocument( NewDocument doc, Map<String,NewMetadata> map,Map<String,Page> pages) {
		if(doc.getAttachments()!=null)
			doc.setAttachments(this.checkMetadata(doc.getAttachments(), map));
		if(doc.getMovies()!=null)
			doc.setMovies(this.checkMetadata(doc.getMovies(), map));
		if(doc.getGallery()!=null)
			doc.setGallery(this.checkMetadata(doc.getGallery(), map));
		if(doc.getPodcasts()!=null)
			doc.setPodcasts(this.checkMetadata(doc.getPodcasts(), map));
		if(doc.getLinks()!=null)
			doc.setLinks(this.checkPage(doc.getLinks(), pages));
		doc.setDescription(this.checkUTF8Text(doc.getDescription()));
		doc.setTitle(this.checkUTF8Text(doc.getTitle()));
	}
	
	public String checkUTF8Text(String text) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
		    char ch = text.charAt(i);
		    if (!Character.isHighSurrogate(ch) && !Character.isLowSurrogate(ch)) {
		        sb.append(ch);
		    }
		}
		return sb.toString();
	}
	
	public void checkSingleDocument( NewDocument doc) {
		Map<String, NewMetadata> map= new HashMap<String,NewMetadata>();
		Map<String, Page> pages=new HashMap<String,Page>();
		this.checkNewDocument(doc, map, pages);
	}
	
	private Set<Page> checkPage(Set<Page> links, Map<String, Page> map) {
		Set<Page> result= new HashSet<Page>();
		for(Page mt:links) {
			 Page aux = map.get(mt.getId());
			if(aux!=null) result.add(aux);
			else {
				map.put(mt.getId(), mt);
				result.add(mt);
			}
		}
			return result;
		
	}
	

	public int  saveOrUpdateAll(List<NewDocument> documents)  {
		Session session = factory.openSession();
		Transaction tx = session.beginTransaction();
		try {
			for (NewDocument  docs : documents) 
                 {
                	 docs.setId(NewDocument.setHashID(docs.getInternal_id().getBytes()));
				     session.saveOrUpdate(docs);
                 }
			tx.commit();
		} catch (RuntimeException e){
			if (tx != null)
				tx.rollback();
			throw e; // or display error message
		}
	
		finally {
			session.close();
		}
      return documents.size();
	}
	
	public NewSearchResult parseSearch(String text, String query, Date from, Date to, int start, int maxresults,
			boolean score, Analyzer analyzer,Boolean links) throws Exception {
		BooleanClause.Occur[] flags = { BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD,
				BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD,
				BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD,
				BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD,
				BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD,
				BooleanClause.Occur.SHOULD };
		String fields[] = { "id", "url", "title", "description", "authors", "type", "source_id", "internal_id",
				"publishDate", "links", "movies", "gallery", "attachments", "podcasts", "score", "neoid", "entities",
				"categories", "trainable" };
		BooleanClause.Occur[] flags_ = {};
		String fields_[] = { "id", "url", "authors", "type", "source_id", "internal_id", "publishDate", "links",
				"movies", "gallery", "attachments", "podcasts", "score", "entities", "trainable" };
		int totaldocs = 0;
		Session session = factory.openSession();
		List<NewDocument> result = new ArrayList<NewDocument>();
		FullTextSession fts = Search.getFullTextSession(session);
		Transaction tx = fts.beginTransaction();
		try {
			QueryBuilder qb = fts.getSearchFactory().buildQueryBuilder().forEntity(NewDocument.class).get();
			Query textQuery = null;

			if (!text.trim().isEmpty())
				textQuery = qb.keyword().onFields("title", "description").matching(text).createQuery();

			Query luceneQuery = null;
			if (!query.trim().isEmpty()) {
				// luceneQuery = queryparser.parse(query);
				luceneQuery = MultiFieldQueryParser.parse(query, fields_, flags, analyzer);
			}
			Query resultquery = null;
			if ((luceneQuery != null) && (textQuery != null))
				resultquery = new BooleanQuery.Builder().add(textQuery, Occur.MUST).add(luceneQuery, Occur.MUST)
						.build();
			else {
				if (luceneQuery != null)
					resultquery = luceneQuery;
				else if (textQuery != null)
					resultquery = textQuery;
			}

			if (from != null || to != null) {
				if (to == null)
					to = Calendar.getInstance().getTime();
				if (from == null) {
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.YEAR, -1);
					from = cal.getTime();
				}
				org.apache.lucene.search.Query rangeQuery = qb.range().onField("publishDate").from(from).to(to)
						.createQuery();
				// wrap Lucene query in a javax.per()sistence.Query
				if(resultquery!=null) {
				resultquery = new BooleanQuery.Builder().add(rangeQuery, BooleanClause.Occur.MUST)
						.add(resultquery, Occur.MUST).build();
				}
				else 
					resultquery=rangeQuery;
			}
			logger.info("query "+resultquery);
			if (score)
				resultquery = new ScoreRankScoreQuery(resultquery);
			// FullTextQuery fullTextQuery = fts.createFullTextQuery(customQuery,
			// Document.class);
			FullTextQuery fullTextQuery = fts.createFullTextQuery(resultquery, NewDocument.class);
			fullTextQuery.setFirstResult(start);
			fullTextQuery.setMaxResults(maxresults);

			// execute search
			result = fullTextQuery.list();
					
					 for(  NewDocument doc:result)
					 { if(links) {
						 Hibernate.initialize(
							 doc.getLinks());
					 }
					   else 
						   doc.setLinks(null);
					 }
			totaldocs = fullTextQuery.getResultSize();
			tx.commit();

		} catch (RuntimeException | ParseException e) {
			if (tx != null)
				tx.rollback();
			throw e; // or display error message
		} finally {
			session.close();
		}

		NewSearchResult searchResult = NewSearchResult.builder().documents(result).totaldocs(totaldocs).build();
		return searchResult;
	}
	public void addAllLuceneDocument(List<org.apache.lucene.document.Document> documents) throws Exception {
		List<NewDocument> docs= new ArrayList<NewDocument>();
		Map<String,NewMetadata> metadatas= new HashMap<String,NewMetadata>();
		Map<String,Page> pages= new HashMap<String,Page>();
		for(org.apache.lucene.document.Document document:documents) {
		NewDocument doc = EntityDocumentBuilder.convertToNewDocument(document,metadatas,pages);
		docs.add(doc);
		}
		this.saveOrUpdateAll(docs);
	}

	public void saveOrUpdateDocument(NewDocument doc) {
		HashMap<String, NewMetadata> map = new HashMap<String,NewMetadata>();
		HashMap<String, Page> pages = new HashMap<String,Page>();
		this.checkNewDocument(doc, map, pages);
		Session session = factory.openSession();
		Transaction tx = session.beginTransaction();
		try {
			
                 session.saveOrUpdate(doc);
			

			tx.commit();
		} catch (RuntimeException e){
			if (tx != null)
				tx.rollback();
			logger.error(""+e+ " "+ doc);
			throw e; // or display error message
		}
	
		finally {
			session.close();
		}

		
	}
	public NewSearchResult dump(Integer start, Integer maxresults, Date from, Date to, Boolean links) {
		Session session = factory.openSession();
		List<NewDocument> result = new ArrayList<NewDocument>();
		FullTextSession fts = Search.getFullTextSession(session);
		Transaction tx = fts.beginTransaction();
		Integer totaldocs = 0;
	
		try {
		if (from != null || to != null) {
			if (to == null)
				to = Calendar.getInstance().getTime();
			if (from == null) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.YEAR, -10);
				from = cal.getTime();
			}
		}
			QueryBuilder qb = fts.getSearchFactory().buildQueryBuilder().forEntity(NewDocument.class).get();
			org.apache.lucene.search.Query rangeQuery = qb.range().onField("timestamp").from(from).to(to)
					.createQuery();
			
			
		
			FullTextQuery fullTextQuery = fts.createFullTextQuery(rangeQuery, NewDocument.class);
			fullTextQuery.setFirstResult(start);
			fullTextQuery.setMaxResults(maxresults);

			// execute search
			
			result = fullTextQuery.list();
			 for(  NewDocument doc:result)
			 { if(links) {
				 Hibernate.initialize(
					 doc.getLinks());
			 }
			   else 
				   doc.setLinks(null);
			 }
			totaldocs = fullTextQuery.getResultSize();
			tx.commit();

		} catch (RuntimeException e) {
			if (tx != null)
				tx.rollback();
			throw e; // or display error message
		} finally {
			session.close();
		}

		NewSearchResult searchResult = NewSearchResult.builder().documents(result).totaldocs(totaldocs).build();
		return searchResult;
	}
	public NewDocument deleteDocument(String id) {
		Session session = factory.openSession();
		FullTextSession fts = Search.getFullTextSession(session);
		Transaction tx = fts.beginTransaction();
		IdentifierLoadAccess<NewDocument> search = fts.byId(NewDocument.class);
		NewDocument doc = search.load(id);
		 if(doc!=null)
			 fts.delete(doc);
	    tx.commit();
		session.close();
		return doc;
		
	}
	public Integer rebuildIndex(Integer firstResult, Integer maxResults) {

		Session session = factory.openSession();
		FullTextSession fullTextSession = Search.getFullTextSession(session);
		fullTextSession.setFlushMode(FlushMode.MANUAL);
		fullTextSession.setCacheMode(CacheMode.IGNORE);
		Transaction transaction = fullTextSession.beginTransaction();
		// Scrollable results will avoid loading too many objects in memory
		ScrollableResults results = fullTextSession.createCriteria(NewDocument.class).setFirstResult(firstResult).setMaxResults(maxResults).setFetchSize(maxResults).scroll(ScrollMode.SCROLL_INSENSITIVE);
		int index = 0;
       Set<NewDocument> h= new HashSet<NewDocument>();
       ArrayList<NewDocument> t= new ArrayList<NewDocument>();
		while (results.next()) {
			NewDocument doc = (NewDocument)results.get(0);
			if(!h.contains(doc)) h.add(doc);
			else t.add(doc);
			fullTextSession.index(doc); // index each element
			index++;
		}
		logger.info( "modified :" +(firstResult+index)+" documents");
		//fullTextSession.flushToIndexes(); // apply changes to indexes
	    //fullTextSession.clear(); // free memory since the queue is processed
		
		transaction.commit();
		session.close();
		return index;
	}
	public void rebuildIndex() throws InterruptedException {
//		int maxResults=10000;
//	
//		int i=0;
//		Integer numdocs = this.size();
//		while(i<numdocs) {
//			Integer totals = this.rebuildIndex( i, maxResults);
//			if (i+maxResults>numdocs) {
//				//i=numdocs;
//				this.rebuildIndex(i, maxResults);
//				i=numdocs;
//			}
//			else i+=totals;
//			
//		}
		Session session = factory.openSession();
		FullTextSession fullTextSession = Search.getFullTextSession(session);
		Transaction tx = fullTextSession.beginTransaction();
		fullTextSession.createIndexer(NewDocument.class )
		.batchSizeToLoadObjects(30 )
		.cacheMode( CacheMode.NORMAL )
		.threadsToLoadObjects( 5 )
		.threadsForSubsequentFetching( 20 )
		.startAndWait();
		tx.commit();
		session.close();
		
	}
	public Long classifyAll(TextClassifier classifier, int start , int maxresults) {
		// EntityManager em = entityManagerFactory.createEntityManager();
		Session session = factory.openSession();
		FullTextSession fts = Search.getFullTextSession(session);
		Transaction tx = fts.beginTransaction();
		QueryBuilder qb = fts.getSearchFactory().buildQueryBuilder().forEntity(NewDocument.class).get();
		org.apache.lucene.search.Query luceneQuery = qb.keyword().onFields("trainable").matching("false").createQuery();
		
		FullTextQuery fullTextQuery = fts.createFullTextQuery(luceneQuery, NewDocument.class);
		fullTextQuery.setFirstResult(start);
		fullTextQuery.setMaxResults(maxresults);
		Iterator<NewDocument> iterator = fullTextQuery.iterate();
		Long i = 0L;
		Long index = 0L;
		while (iterator.hasNext()) {
			NewDocument doc = iterator.next();

			List<ScoredItem> result;
			try {
				result = classifier.classify(doc);

				String[] categories = DocumentService.firstTwoCategories(result);

				if (categories != null) {
					doc.setCategories(categories);
					session.update(doc);
					i++;
				}

			} catch (IOException e) {
				logger.error(""+e);
			}
			
			index++;
		}
		tx.commit();
		session.close();
		logger.info( "classified :" +(start+index)+" documents");
		return index;

	}
	public NewDocument getById(String id,Boolean links) {
		Session session = factory.openSession();
		FullTextSession fts = Search.getFullTextSession(session);
		Transaction tx = fts.beginTransaction();
		IdentifierLoadAccess<NewDocument> search = fts.byId(NewDocument.class);
		NewDocument doc = search.load(id);
		 if(links)  Hibernate.initialize(doc.getLinks());
		tx.commit();
		session.close();
		return doc;
	}
	
	public NewSearchResult searchDocument(String text, Integer start, Integer maxresults, Date from, Date to) {
		Session session = factory.openSession();
		List<NewDocument> result = new ArrayList<NewDocument>();
		FullTextSession fts = Search.getFullTextSession(session);
		Transaction tx = fts.beginTransaction();
		Integer totaldocs = 0;
		// create native Lucene query unsing the query DSL
		// alternatively you can write the Lucene query using the Lucene query
		// parser
		// or the Lucene programmatic API. The Hibernate Search DSL is
		// recommended though
		try {
			QueryBuilder qb = fts.getSearchFactory().buildQueryBuilder().forEntity(NewDocument.class).get();
			org.apache.lucene.search.Query luceneQuery = qb.keyword().onFields("title", "description", "authors")
					.matching(text).createQuery();
			BooleanQuery.Builder bool = new BooleanQuery.Builder();
			bool.add(luceneQuery, BooleanClause.Occur.MUST);

			if (from != null || to != null) {
				if (to == null)
					to = Calendar.getInstance().getTime();
				if (from == null) {
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.YEAR, -1);
					from = cal.getTime();
				}
				org.apache.lucene.search.Query rangeQuery = qb.range().onField("publishDate").from(from).to(to)
						.createQuery();
				// wrap Lucene query in a javax.per()sistence.Query
				bool.add(rangeQuery, BooleanClause.Occur.MUST);
			}

			FullTextQuery fullTextQuery = fts.createFullTextQuery(bool.build(), NewDocument.class);

			fullTextQuery.setFirstResult(start);
			fullTextQuery.setMaxResults(maxresults);

			// execute search
			result = fullTextQuery.list();

			totaldocs = fullTextQuery.getResultSize();
			tx.commit();
		} catch (RuntimeException e) {
			if (tx != null)
				tx.rollback();
			throw e; // or display error message
		} finally {
			session.close();
		}
		NewSearchResult searchResult = NewSearchResult.builder().documents(result).totaldocs(totaldocs).build();
		return searchResult;
	}

	public NewSearchResult semanticSearch(String text, PageRankResult pageRankResult, int start, int maxresults) {
		Integer totaldocs = 0;
		Session session = factory.openSession();
		FullTextSession fts = Search.getFullTextSession(session);
		Transaction tx = fts.beginTransaction();

		// create native Lucene query unsing the query DSL
		// alternatively you can write the Lucene query using the Lucene query
		// parser
		// or the Lucene programmatic API. The Hibernate Search DSL is
		// recommended though
		QueryBuilder qb = fts.getSearchFactory().buildQueryBuilder().forEntity(NewDocument.class).get();
		org.apache.lucene.search.Query luceneQuery = qb.keyword().onFields("title", "description", "authors")
				.matching(text).createQuery();
		EntityRankScoreQuery customQuery = new EntityRankScoreQuery(luceneQuery, pageRankResult);
		FullTextQuery fullTextQuery = fts.createFullTextQuery(customQuery, NewDocument.class);

		fullTextQuery.setFirstResult(start);
		fullTextQuery.setMaxResults(maxresults);

		// execute search
		List<NewDocument> result = fullTextQuery.list();
		totaldocs = fullTextQuery.getResultSize();
		tx.commit();
		session.close();
		NewSearchResult searchResult = NewSearchResult.builder().documents(result).totaldocs(totaldocs).build();
		return searchResult;
	}

	public Long detectKeywords(Set<String> keywords) {

		Session session = factory.openSession();
		FullTextSession fullTextSession = Search.getFullTextSession(session);
		fullTextSession.setFlushMode(FlushMode.MANUAL);
		fullTextSession.setCacheMode(CacheMode.IGNORE);
		Transaction transaction = fullTextSession.beginTransaction();
		// Scrollable results will avoid loading too many objects in memory
		ScrollableResults results = fullTextSession.createCriteria(NewDocument.class).setFetchSize(BATCH_SIZE)
				.scroll(ScrollMode.FORWARD_ONLY);
		Long index = 0L;
		Analyzer analyzer = new ShingleAnalyzerWrapper(2, 3);
        Date thistime = Calendar.getInstance().getTime();
		while (results.next()) {
			NewDocument doc = (NewDocument) results.get(0);
			index++;
			String[] keyws;
			try {
				keyws = DocumentService.keywordsDetect(doc.text(), keywords, analyzer);
				doc.setKeywords(keyws);
				doc.setTimestamp(thistime);
			    fullTextSession.update(results.get(0));
			  
			} catch (IOException e) {
				logger.error(""+e);
			}

			if (index % BATCH_SIZE == 0) {
				logger.info( "modified :" +(index)+" documents");
				fullTextSession.clear(); // free memory since the queue is processed
				
			}
			
		}
		transaction.commit();
		session.close();
		return index;

	}
	public Integer addDocuments(InputStream inputStream) throws Exception {

		InputStreamReader reader = new InputStreamReader(inputStream);


		ObjectMapper objectMapper = new ObjectMapper();
		//DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      //objectMapper.setDateFormat(df);
		List<NewDocument> documents = objectMapper.readValue(reader, new TypeReference<List<NewDocument>>() {
		});
		this.assignIdentifiers(documents);
		this.checkDocuments(documents);
		try {
			
		
		this.saveOrUpdateAll(documents);
		}
		catch (RuntimeException e){
			for( NewDocument doc:documents)
				this.saveOrUpdateDocument(doc);
		}
		return documents.size();
	}

	public Integer size() {
//		int result;
//		Session session = factory.openSession();
//		FullTextSession fts = Search.getFullTextSession(session);
//		Transaction tx = fts.beginTransaction();
//		Statistics statistics = fts.getSearchFactory().getStatistics();
//		result = statistics.getNumberOfIndexedEntities(NewDocument.class.getName());
//		tx.commit();
//		session.close();
//		
//		return result;
		Session session = factory.openSession();
		FullTextSession fts = Search.getFullTextSession(session);
		Transaction tx = fts.beginTransaction();
		Criteria criteriaCount = session.createCriteria(NewDocument.class);
		criteriaCount.setProjection(Projections.rowCount());
		Long count = (Long) criteriaCount.uniqueResult();
		tx.commit();
		session.close();
		return count.intValue();
	}
	public Integer indexsize() {
		int result;
		Session session = factory.openSession();
		FullTextSession fts = Search.getFullTextSession(session);
		Transaction tx = fts.beginTransaction();
		Statistics statistics = fts.getSearchFactory().getStatistics();
		result = statistics.getNumberOfIndexedEntities(NewDocument.class.getName());
		tx.commit();
		session.close();
		
		return result;
	}
	public List<Documentable> getTrainable() {
		Session session = factory.openSession();
		FullTextSession fts = Search.getFullTextSession(session);
		Transaction tx = fts.beginTransaction();
		QueryBuilder qb = fts.getSearchFactory().buildQueryBuilder().forEntity(NewDocument.class).get();
		org.apache.lucene.search.Query luceneQuery = qb.keyword().onFields("trainable").matching("true").createQuery();
		FullTextQuery fullTextQuery = fts.createFullTextQuery(luceneQuery, NewDocument.class);
		List<Documentable> result = fullTextQuery.list();
		tx.commit();
		session.close();
		return result;
	}
	public Long classifyAll(TextClassifier classifier)
	{    int big_batch=1000;
	     int start=0;
	     Long total=0L;
	     Long result = this.classifyAll(classifier, start, big_batch);
	     total=result;
		 while(result>0) {
			 start=start+big_batch;
			 result = this.classifyAll(classifier, start, big_batch);
			 total+=result;
		 }
		 return total;
	}
	public void migrate(NewDocumentService service, TextClassifier textClassifier, Set<String> keywords) {
		 int maxdocs=Integer.MAX_VALUE;
		 //int maxdocs=10000;
		Session session = factory.openSession();
		
		FullTextSession fullTextSession = Search.getFullTextSession(session);
		fullTextSession.setFlushMode(FlushMode.MANUAL);
		fullTextSession.setCacheMode(CacheMode.IGNORE);
		Transaction transaction = fullTextSession.beginTransaction();
		// Scrollable results will avoid loading too many objects in memory
		
		ScrollableResults results = fullTextSession.createCriteria(NewDocument.class).setFetchSize(BATCH_SIZE)
				.scroll(ScrollMode.FORWARD_ONLY);
		int index = 0;
		List<NewDocument> docs = new ArrayList<NewDocument>();
		Analyzer analyzer = new ShingleAnalyzerWrapper(2, 3);
       Date thistime = Calendar.getInstance().getTime();
		while (results.next()&&index<maxdocs) {
			index++;
			NewDocument docaux ;
			docaux=(NewDocument)results.get(0);
			docs.add(docaux);
			if (index % BATCH_SIZE == 0) {
				
				try {
					for(NewDocument doc:docs) {
					List<ScoredItem> result = textClassifier.classify(doc);
					//doc.setId(null);
					String[] categories = DocumentService.firstTwoCategories(result);
					doc.setCategories(categories);
					String[] keys = DocumentService.keywordsDetect(doc.text(), keywords, analyzer);
					doc.setKeywords(keys);
					doc.setTimestamp(thistime);
					}
					 service. saveAll(docs);
					 
					logger.info("added " + index + " documents");
				} catch (Exception e) {
					logger.error("skipped " + index + " documents");
				}
				docs = new ArrayList<NewDocument>();
				//fullTextSession.flush();
				fullTextSession.clear(); // free memory since the queue is processed
			}
		}
	
		transaction.commit();
	
		session.close();
		logger.info("migration terminated");
		//System.out.println("exit");
	}
	
	public void buildAuthors(AuthorService service, int firstResult,int maxResults) {

		Session session = factory.openSession();
		FullTextSession fullTextSession = Search.getFullTextSession(session);
		fullTextSession.setFlushMode(FlushMode.MANUAL);
		fullTextSession.setCacheMode(CacheMode.IGNORE);
		Transaction transaction = fullTextSession.beginTransaction();
		// Scrollable results will avoid loading too many objects in memory
		int batch_size=maxResults/10;
		ScrollableResults results = fullTextSession.createCriteria(NewDocument.class).setFirstResult(firstResult).setMaxResults(maxResults).setFetchSize(batch_size)
				.scroll(ScrollMode.FORWARD_ONLY);
		int index = 0;
		Map<String,Author> authors = new HashMap<String,Author>();
		Analyzer analyzer = new CommaAnalyzer();
		while (results.next()) {
			index++;
			// fullTextSession.index(results.get(0)); // index each element
			NewDocument doc = (NewDocument) results.get(0);
			List<String> splitted;
			try {
				if (doc.getAuthors() != null) {
					splitted = DocumentService.splittedAuthors(doc.getAuthors(), analyzer);

					for (String sp : splitted) {
						if (!sp.trim().isEmpty()) {
							Author author = Author.builder().id(DigestUtils.md5Hex(sp)).authors(sp).authors_(sp).frequency(1)
									.build();
							Author aux;
							if((aux=authors.get(author.getAuthors()))!=null) {
								aux.setFrequency(aux.getFrequency()+1);
							  // authors.put(author.getAuthors(),aux);
							}
							else  authors.put(author.getAuthors(),author);
						}
					}
				}
			} catch (IOException e1) {
				logger.info(e1);

			}
			if (index %batch_size == 0) {
				// fullTextSession.flushToIndexes(); // apply changes to indexes
				try {
					service.updateFrequencyAllAuthors(authors.values());
				    authors = new HashMap<String,Author>();
				    logger.info("build authors from "+ (firstResult+index )+ "documents");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				authors = new HashMap<String,Author>();
				fullTextSession.clear(); // free memory since the queue is processed
			}
		}
		transaction.commit();
		session.close();
	}

	public void buildAuthors(AuthorService authorService) {
		Integer numdocs = this.size();
		int maxResults=10000;
		int k=0;
		while(k<numdocs) {
			this.buildAuthors(authorService, k, maxResults);
			if (k+maxResults>numdocs) {
				k=numdocs;
				this.buildAuthors(authorService, k, maxResults);
			}
			else k+=maxResults;
			
		}
		
	}

	public NewDocument getNaturald(Long id,Boolean links) {
		Session session = factory.openSession();
		//FullTextSession fts = Search.getFullTextSession(session);
		Transaction tx = session.beginTransaction();
	
 NewDocument doc = session.byNaturalId(NewDocument.class).using("id", id.longValue()).load();
	//	NewDocument doc = search.load();
      if(links)  Hibernate.initialize(doc.getLinks());
      else doc.setLinks(null);
		tx.commit();
		session.close();
		return doc;
	}
	
	public List<Documentable> trainsetUsingSource(Map<String, String> sourceidCat, int max) {

		Integer size = this.size();
		int index=0;
		List<Documentable> result=new ArrayList<Documentable>();
		Map <String,Integer> catmax= new HashMap<String,Integer>();
		int maxResults=100;
		//Integer totcat=sourceidCat.values().size()*max;
		while (index<size&&index<1000000) {
		
		    List<Documentable> docs = this.getDocuments(index, maxResults);
		for(Documentable doc:docs) {
			
		NewDocument current = (NewDocument) doc;
			if (current.getSource_id() != null) {
				String category = sourceidCat.get(current.getSource_id());
				Integer tot=catmax.get(category);
				if(tot==null) {
					catmax.put(category, 0);
					tot=0;
				}
				
				
				if (tot<max&&category != null) {
					String[] categories = new String[1];
					categories[0]=category.toLowerCase();
					current.setCategories(categories);
					result.add(current);
					 catmax.put(category,tot+1);
				}
			}


		}
		if(index+maxResults>size) index=size;
		else index+=maxResults;
		logger.info("totals "+ index);
		}

		return result;
	}
	public List<Documentable> getDocuments(int firstResult, int maxResults){
		Session session = factory.openSession();
		FullTextSession fullTextSession = Search.getFullTextSession(session);
		fullTextSession.setFlushMode(FlushMode.MANUAL);
		fullTextSession.setCacheMode(CacheMode.IGNORE);
		Transaction transaction = fullTextSession.beginTransaction();
		List<Documentable> docs = new ArrayList<Documentable>();
		ScrollableResults results = fullTextSession.createCriteria(NewDocument.class).setFirstResult(firstResult).setMaxResults(maxResults).setFetchSize(maxResults)
				.scroll(ScrollMode.FORWARD_ONLY);
		int index = 0;
		while (results.next()) {
			index++;
			NewDocument current = (NewDocument) results.get(0);
	        docs.add(current);
		}
		transaction.commit();
		return docs;
	}
	
	public void optimizeIndex() {
		Session session = factory.openSession();
		FullTextSession fullTextSession = Search.getFullTextSession(session);
		Transaction transaction = fullTextSession.beginTransaction();
		SearchFactory searchFactory = fullTextSession.getSearchFactory();
		searchFactory.optimize();
		transaction.commit();
	
		
	}
}
