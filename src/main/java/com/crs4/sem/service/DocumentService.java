package com.crs4.sem.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.IdentifierLoadAccess;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.MassIndexer;
import org.hibernate.search.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.stat.Statistics;
import org.hibernate.service.ServiceRegistry;
import org.neo4j.graphalgo.impl.PageRankResult;

import com.crs4.sem.analysis.CommaAnalyzer;
import com.crs4.sem.exceptions.NotUniqueDocumentException;
import com.crs4.sem.lucene.similarity.EntityRankScoreQuery;
import com.crs4.sem.lucene.similarity.ScoreRankScoreQuery;
import com.crs4.sem.model.Author;
import com.crs4.sem.model.Document;
import com.crs4.sem.model.Documentable;
import com.crs4.sem.model.NewDocument;
import com.crs4.sem.model.SearchResult;
import com.crs4.sem.model.Shado;
import com.crs4.sem.model.lucene.EntityDocumentBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mfl.sem.classifier.text.TextClassifier;
import com.mfl.sem.model.ScoredItem;

import lombok.Data;

@Data
public class DocumentService extends HibernateService {
	public static final Logger logger = Logger.getLogger(DocumentService.class);

	public DocumentService(String connection_url, String lucene_indexbase, String directory_provider) {

		Configuration configuration = new Configuration();
		configuration = configuration.addAnnotatedClass(Document.class);
		configuration = configuration.configure();
		configuration.setProperty("hibernate.search.default.directory_provider", directory_provider);
		configuration.setProperty("hibernate.connection.url", connection_url);
		configuration.setProperty("hibernate.search.default.indexBase", lucene_indexbase);
		ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
				.applySettings(configuration.getProperties()).build();
		factory = configuration.buildSessionFactory(serviceRegistry);
	};

	public DocumentService(Configuration configuration) {
		super(configuration);
	};

	public SearchResult searchDocument(String text, Integer start, Integer maxresults, Date from, Date to) {
		Session session = factory.openSession();
		List<Document> result = new ArrayList<Document>();
		FullTextSession fts = Search.getFullTextSession(session);
		Transaction tx = fts.beginTransaction();
		Integer totaldocs = 0;
		// create native Lucene query unsing the query DSL
		// alternatively you can write the Lucene query using the Lucene query
		// parser
		// or the Lucene programmatic API. The Hibernate Search DSL is
		// recommended though
		try {
			QueryBuilder qb = fts.getSearchFactory().buildQueryBuilder().forEntity(Document.class).get();
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

			FullTextQuery fullTextQuery = fts.createFullTextQuery(bool.build(), Document.class);

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
		SearchResult searchResult = SearchResult.builder().documents(result).totaldocs(totaldocs).build();
		return searchResult;
	}

	public SearchResult semanticSearch(String text, PageRankResult pageRankResult, int start, int maxresults) {
		Integer totaldocs = 0;
		Session session = factory.openSession();
		FullTextSession fts = Search.getFullTextSession(session);
		Transaction tx = fts.beginTransaction();

		// create native Lucene query unsing the query DSL
		// alternatively you can write the Lucene query using the Lucene query
		// parser
		// or the Lucene programmatic API. The Hibernate Search DSL is
		// recommended though
		QueryBuilder qb = fts.getSearchFactory().buildQueryBuilder().forEntity(Document.class).get();
		org.apache.lucene.search.Query luceneQuery = qb.keyword().onFields("title", "description", "authors")
				.matching(text).createQuery();
		EntityRankScoreQuery customQuery = new EntityRankScoreQuery(luceneQuery, pageRankResult);
		FullTextQuery fullTextQuery = fts.createFullTextQuery(customQuery, Document.class);

		fullTextQuery.setFirstResult(start);
		fullTextQuery.setMaxResults(maxresults);

		// execute search
		List<Document> result = fullTextQuery.list();
		totaldocs = fullTextQuery.getResultSize();
		tx.commit();
		session.close();
		SearchResult searchResult = SearchResult.builder().documents(result).totaldocs(totaldocs).build();
		return searchResult;
	}

	public SearchResult parseSearch(String text, String query, Date from, Date to, int start, int maxresults,
			boolean score, Analyzer analyzer) throws Exception {
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
		List<Document> result = new ArrayList<Document>();
		FullTextSession fts = Search.getFullTextSession(session);
		Transaction tx = fts.beginTransaction();
		try {
			QueryBuilder qb = fts.getSearchFactory().buildQueryBuilder().forEntity(Document.class).get();
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
			FullTextQuery fullTextQuery = fts.createFullTextQuery(resultquery, Document.class);
			fullTextQuery.setFirstResult(start);
			fullTextQuery.setMaxResults(maxresults);

			// execute search
			result = fullTextQuery.list();
			totaldocs = fullTextQuery.getResultSize();
			tx.commit();

		} catch (RuntimeException | ParseException e) {
			if (tx != null)
				tx.rollback();
			throw e; // or display error message
		} finally {
			session.close();
		}

		SearchResult searchResult = SearchResult.builder().documents(result).totaldocs(totaldocs).build();
		return searchResult;
	}

//	public List<Document> searchSimilarDocuments(Document doc, int start, int maxresults){
//		Session session = factory.openSession();
//		FullTextSession fts = Search.getFullTextSession(session);
//		Transaction tx = fts.beginTransaction();
//		QueryBuilder qb = fts.getSearchFactory().buildQueryBuilder().forEntity(Document.class).get();
//		org.apache.lucene.search.Query luceneQuery = qb.keyword().onFields("title","description").matching(doc.getTitle()+doc.getDescription()).createQuery();
//		org.apache.lucene.search.Query luceneQuery_authors = qb.keyword().onFields("authors").matching(doc.getAuthors()).createQuery();
//	}
	public Long addDocument(Document doc) throws Exception {
		Session session = factory.openSession();
		Transaction tx = session.beginTransaction();
		Document auxdoc = null;
		try {

			auxdoc = addDoc(doc, session);
			tx.commit();

		} catch (RuntimeException e) {
			
			if (tx != null)
				tx.rollback();
			throw e;
		}
			catch(NotUniqueDocumentException e) {
				logger.info(""+e);// or display error message
			}
		 finally {
			session.close();
		}
		return auxdoc.getId();
	}

	private Document addDocNoTest(Document doc, Session session) throws NotUniqueDocumentException {
		
				Long id = (Long) session.save(doc);
				doc.setId(id);
				return doc;
			
	}
	private Document addDoc(Document doc, Session session) throws NotUniqueDocumentException {
		Document auxdoc = null;
		if (doc.getInternal_id() != null) {
			auxdoc = getDocId(doc, session);
			if (auxdoc != null) {
				auxdoc.copyFields(doc);
				session.update(auxdoc);
				return auxdoc;
			} else {
				Long id = (Long) session.save(doc);
				doc.setId(id);
				return doc;
			}
		} else {
			Long id = (Long) session.save(doc);
			doc.setId(id);
			return doc;
		}

	}

	private Document getDocId(Document doc, Session session) throws NotUniqueDocumentException {
		FullTextSession fts = Search.getFullTextSession(session);
		QueryBuilder qb = fts.getSearchFactory().buildQueryBuilder().forEntity(Document.class).get();
		Query luceneQuery = qb.keyword().onField("internal_id").matching(doc.getInternal_id()).createQuery();
		FullTextQuery fullTextQuery = fts.createFullTextQuery(luceneQuery, Document.class);
		List<Document> docs = fullTextQuery.list();
		if (docs.size() > 1)
			throw new NotUniqueDocumentException();
		else if (docs.size() == 1)
			return docs.get(0);

		return null;
	}

	public void updateDocument(Document doc) {
		Session session = factory.openSession();
		// FullTextSession fts =Search.getFullTextSession(session);
		// fts.createIndexer().startAndWait();
		Transaction tx = session.beginTransaction();

		session.update(doc);
		tx.commit();
		session.close();

	}

	public Document getById(Long id) {
		Session session = factory.openSession();
		FullTextSession fts = Search.getFullTextSession(session);
		Transaction tx = fts.beginTransaction();
		IdentifierLoadAccess<Document> search = fts.byId(Document.class);
		Document doc = search.load(id);
		tx.commit();
		session.close();
		return doc;
	}

	public void addAllDocument(List<Document> documents) throws Exception {
		Session session = factory.openSession();
		Transaction tx = session.beginTransaction();
		try {
			for (Document doc : documents)

				this.addDoc(doc, session);

			tx.commit();
		} catch (RuntimeException e){
			if (tx != null)
				tx.rollback();
			throw e; // or display error message
		}
	catch(NotUniqueDocumentException e) {
		logger.info(""+e);// or display error message
	}
		finally {
			session.close();
		}

	}
	public void addAllDocumentNoTest(List<Document> documents) throws Exception {
		Session session = factory.openSession();
		Transaction tx = session.beginTransaction();
		try {
			for (Document doc : documents)

				this.addDocNoTest(doc, session);

			tx.commit();
		} catch (RuntimeException | NotUniqueDocumentException e) {
			if (tx != null)
				tx.rollback();
			throw e; // or display error message
		} finally {
			session.close();
		}

	}

	public List<Documentable> getTrainable() {
		Session session = factory.openSession();
		FullTextSession fts = Search.getFullTextSession(session);
		Transaction tx = fts.beginTransaction();
		QueryBuilder qb = fts.getSearchFactory().buildQueryBuilder().forEntity(Document.class).get();
		org.apache.lucene.search.Query luceneQuery = qb.keyword().onFields("trainable").matching("true").createQuery();
		FullTextQuery fullTextQuery = fts.createFullTextQuery(luceneQuery, Document.class);
		List<Documentable> result = fullTextQuery.list();
		tx.commit();
		session.close();
		return result;
	}

	public List<Documentable> trainsetUsingSource(Map<String, String> sourceidCat) {

		Session session = factory.openSession();
		FullTextSession fullTextSession = Search.getFullTextSession(session);
		fullTextSession.setFlushMode(FlushMode.MANUAL);
		fullTextSession.setCacheMode(CacheMode.IGNORE);
		Transaction transaction = fullTextSession.beginTransaction();
		List<Documentable> docs = new ArrayList<Documentable>();
		ScrollableResults results = fullTextSession.createCriteria(Document.class).setFetchSize(BATCH_SIZE)
				.scroll(ScrollMode.FORWARD_ONLY);
		int index = 0;
		while (results.next()) {
			index++;
			Document current = (Document) results.get(0);
			// fullTextSession.index( results.get(0) ); //index each element
			if (current.getSource_id() != null) {
				String category = sourceidCat.get(current.getSource_id());
				if (category != null) {
					String[] categories = new String[1];
					current.setCategories(categories);
					docs.add(current);
				}
			}

			if (index % BATCH_SIZE == 0) {
				fullTextSession.flushToIndexes(); // apply changes to indexes
				fullTextSession.clear(); // free memory since the queue is processed
			}
		}
		transaction.commit();
		return docs;
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
	public Long classifyAll(TextClassifier classifier, int start , int maxresults) {
		// EntityManager em = entityManagerFactory.createEntityManager();
		Session session = factory.openSession();
		FullTextSession fts = Search.getFullTextSession(session);
		Transaction tx = fts.beginTransaction();
		QueryBuilder qb = fts.getSearchFactory().buildQueryBuilder().forEntity(Document.class).get();
		org.apache.lucene.search.Query luceneQuery = qb.keyword().onFields("trainable").matching("false").createQuery();
		
		FullTextQuery fullTextQuery = fts.createFullTextQuery(luceneQuery, Document.class);
		fullTextQuery.setFirstResult(start);
		fullTextQuery.setMaxResults(maxresults);
		Iterator<Document> iterator = fullTextQuery.iterate();
		Long i = 0L;
		Long index = 0L;
		List<Document>  docs= new ArrayList<Document>();
		while (iterator.hasNext()) {
			Document doc = iterator.next();

			List<ScoredItem> result;
			try {
				result = classifier.classify(doc);

				String[] categories = firstTwoCategories(result);

				if (categories != null) {
					doc.setCategories(categories);
					//session.update(doc);
					docs.add(doc);
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

	public static String[] firstTwoCategories(List<ScoredItem> result) {
		String categories[] = null;
		if (result.size() >= 2) {
			categories = new String[2];
			categories[0] = result.get(0).getLabel();
			categories[1] = result.get(1).getLabel();
		} else if (result.size() == 1) {
			categories = new String[1];
			categories[0] = result.get(0).getLabel();
		}
		return categories;
	}

	
	
	public Long detectKeywords(Set<String> keywords) {

		Session session = factory.openSession();
		FullTextSession fullTextSession = Search.getFullTextSession(session);
		fullTextSession.setFlushMode(FlushMode.MANUAL);
		fullTextSession.setCacheMode(CacheMode.IGNORE);
		Transaction transaction = fullTextSession.beginTransaction();
		// Scrollable results will avoid loading too many objects in memory
		ScrollableResults results = fullTextSession.createCriteria(Document.class).setFetchSize(BATCH_SIZE)
				.scroll(ScrollMode.FORWARD_ONLY);
		Long index = 0L;
		Analyzer analyzer = new ShingleAnalyzerWrapper(2, 3);
        Date thistime = Calendar.getInstance().getTime();
		while (results.next()) {
			Document doc = (Document) results.get(0);
			index++;
			String[] keyws;
			try {
				keyws = keywordsDetect(doc.text(), keywords, analyzer);
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

	public DocumentService() {
		super();
	}

	public Integer size() {
		int result;
		Session session = factory.openSession();
		FullTextSession fts = Search.getFullTextSession(session);
		Transaction tx = fts.beginTransaction();
		Statistics statistics = fts.getSearchFactory().getStatistics();
		result = statistics.getNumberOfIndexedEntities(Document.class.getName());
		tx.commit();
		session.close();
		return result;
	}

	public Integer addDocuments(InputStream inputStream) throws Exception {

		InputStreamReader reader = new InputStreamReader(inputStream);

		ObjectMapper objectMapper = new ObjectMapper();

		List<Document> documents = objectMapper.readValue(reader, new TypeReference<List<Document>>() {
		});
		this.addAllDocument(documents);
		return documents.size();
	}

	public void dumpIndex(File file) {

	}

	public void rebuildIndex() {

		Session session = factory.openSession();
		FullTextSession fullTextSession = Search.getFullTextSession(session);
		fullTextSession.setFlushMode(FlushMode.MANUAL);
		fullTextSession.setCacheMode(CacheMode.IGNORE);
		Transaction transaction = fullTextSession.beginTransaction();
		// Scrollable results will avoid loading too many objects in memory
		ScrollableResults results = fullTextSession.createCriteria(Document.class).setFetchSize(BATCH_SIZE)
				.scroll(ScrollMode.FORWARD_ONLY);
		int index = 0;

		while (results.next()) {
			index++;
			fullTextSession.index(results.get(0)); // index each element
			if (index % BATCH_SIZE == 0) {
				logger.info( "modified :" +index+" documents");
				fullTextSession.flushToIndexes(); // apply changes to indexes
				fullTextSession.clear(); // free memory since the queue is processed
			}
		}
		transaction.commit();
		session.close();
	}

	public void rebuildMassIndex() throws InterruptedException {
		Session session = factory.openSession();
		FullTextSession fullTextSession = Search.getFullTextSession(session);

		MassIndexer massIndexer = fullTextSession.createIndexer();
		massIndexer.startAndWait();
		session.close();
	}

	public void migrate(DocumentService service, Boolean unique) {
		 int maxdocs=Integer.MAX_VALUE;
		Session session = factory.openSession();
		FullTextSession fullTextSession = Search.getFullTextSession(session);
		fullTextSession.setFlushMode(FlushMode.MANUAL);
		fullTextSession.setCacheMode(CacheMode.IGNORE);
		Transaction transaction = fullTextSession.beginTransaction();
		// Scrollable results will avoid loading too many objects in memory
		
		ScrollableResults results = fullTextSession.createCriteria(Document.class).setFetchSize(BATCH_SIZE)
				.scroll(ScrollMode.FORWARD_ONLY);
		int index = 0;
		List<Document> docs = new ArrayList<Document>();
		while (results.next()&&index<maxdocs) {
			index++;
			// fullTextSession.index(results.get(0)); // index each element
			docs.add((Document) results.get(0));
			if (index % BATCH_SIZE == 0) {
				// fullTextSession.flushToIndexes(); // apply changes to indexes
				try {
					if (!unique) service.addAllDocumentNoTest(docs);
					else service.addAllDocument(docs);
					logger.info("added " + index + " documents");
				} catch (Exception e) {
					logger.error("skipped " + index + " documents");
				}
				docs = new ArrayList<Document>();
				//fullTextSession.flush();
				fullTextSession.clear(); // free memory since the queue is processed
			}
		}
		//System.out.println("1close");
		transaction.commit();
		//System.out.println("2close");
		session.close();
		//System.out.println("3close");
	}

	public void buildAuthors(AuthorService service) {

		Session session = factory.openSession();
		FullTextSession fullTextSession = Search.getFullTextSession(session);
		fullTextSession.setFlushMode(FlushMode.MANUAL);
		fullTextSession.setCacheMode(CacheMode.IGNORE);
		Transaction transaction = fullTextSession.beginTransaction();
		// Scrollable results will avoid loading too many objects in memory
		ScrollableResults results = fullTextSession.createCriteria(Document.class).setFetchSize(BATCH_SIZE)
				.scroll(ScrollMode.FORWARD_ONLY);
		int index = 0;
		Map<String,Author> authors = new HashMap<String,Author>();
		Analyzer analyzer = new CommaAnalyzer();
		while (results.next()) {
			index++;
			// fullTextSession.index(results.get(0)); // index each element
			Document doc = (Document) results.get(0);
			List<String> splitted;
			try {
				if (doc.getAuthors() != null) {
					splitted = this.splittedAuthors(doc.getAuthors(), analyzer);

					for (String sp : splitted) {
						if (!sp.trim().isEmpty()) {
							Author author = Author.builder().authors(sp).authors_(sp).frequency(1)
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
			if (index % BATCH_SIZE == 0) {
				// fullTextSession.flushToIndexes(); // apply changes to indexes
				try {
					service.addAllAuthors(authors.values());
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

	public static  List<String> splittedAuthors(String text, Analyzer analyzer) throws IOException {
		List<String> result = new ArrayList<String>();

		TokenStream stream = analyzer.tokenStream(null, new StringReader(text));
		stream.reset();
		while (stream.incrementToken()) {
			result.add(stream.getAttribute(CharTermAttribute.class).toString());
		}
		stream.close();
		return result;
	}

	public static String[] keywordsDetect(String text, Set<String> keywords, Analyzer analyzer) throws IOException {

		Set<String> result = new HashSet<String>();
		TokenStream stream = analyzer.tokenStream(null, new StringReader(text));
		stream.reset();
		while (stream.incrementToken()) {
			String k = stream.getAttribute(CharTermAttribute.class).toString();
			if (keywords.contains(k))
				result.add(k);

		}

		stream.close();
		String aux[] = null;
		if (result.size() > 0) {
			aux = new String[result.size()];
			result.toArray(aux);
		}
		return aux;

	}

	public SearchResult dump(Integer start, Integer maxresults, Date from, Date to) {
		Session session = factory.openSession();
		List<Document> result = new ArrayList<Document>();
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
			QueryBuilder qb = fts.getSearchFactory().buildQueryBuilder().forEntity(Document.class).get();
			org.apache.lucene.search.Query rangeQuery = qb.range().onField("timestamp").from(from).to(to)
					.createQuery();
			
			
		
			FullTextQuery fullTextQuery = fts.createFullTextQuery(rangeQuery, Document.class);
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

		SearchResult searchResult = SearchResult.builder().documents(result).totaldocs(totaldocs).build();
		return searchResult;
	}

	

	public void migrate(DocumentService service, TextClassifier textClassifier, Set<String> keywords) {
		 int maxdocs=Integer.MAX_VALUE;
		 //int maxdocs=10000;
		Session session = factory.openSession();
		
		FullTextSession fullTextSession = Search.getFullTextSession(session);
		fullTextSession.setFlushMode(FlushMode.MANUAL);
		fullTextSession.setCacheMode(CacheMode.IGNORE);
		Transaction transaction = fullTextSession.beginTransaction();
		// Scrollable results will avoid loading too many objects in memory
		
		ScrollableResults results = fullTextSession.createCriteria(Document.class).setFetchSize(BATCH_SIZE)
				.scroll(ScrollMode.FORWARD_ONLY);
		int index = 0;
		List<Document> docs = new ArrayList<Document>();
		Analyzer analyzer = new ShingleAnalyzerWrapper(2, 3);
        Date thistime = Calendar.getInstance().getTime();
		while (results.next()&&index<maxdocs) {
			index++;
			Document docaux ;
			docaux=(Document)results.get(0);
			docs.add(docaux);
			if (index % BATCH_SIZE == 0) {
				
				try {
					for(Document doc:docs) {
					List<ScoredItem> result = textClassifier.classify(doc);
					doc.setId(null);
					String[] categories = firstTwoCategories(result);
					doc.setCategories(categories);
					String[] keys = keywordsDetect(doc.text(), keywords, analyzer);
					doc.setKeywords(keys);
					doc.setTimestamp(thistime);
					}
					 service.addAllDocument(docs);
					 
					logger.info("added " + index + " documents");
				} catch (Exception e) {
					logger.error("skipped " + index + " documents");
				}
				docs = new ArrayList<Document>();
				//fullTextSession.flush();
				fullTextSession.clear(); // free memory since the queue is processed
			}
		}
	
		transaction.commit();
	
		session.close();
		logger.info("migration terminated");
		System.out.println("exit");
	}

	public Document deleteDocument(Long id) {
		Session session = factory.openSession();
		FullTextSession fts = Search.getFullTextSession(session);
		Transaction tx = fts.beginTransaction();
		IdentifierLoadAccess<Document> search = fts.byId(Document.class);
		Document doc = search.load(id);
		 if(doc!=null)
			 fts.delete(doc);
	    tx.commit();
		session.close();
		return doc;
		
	}

	public void addLuceneDocument(org.apache.lucene.document.Document document) throws Exception {
		Document doc = EntityDocumentBuilder.convert(document);
		this.addDocument(doc);
	}
	
	public void addAllLuceneDocument(List<org.apache.lucene.document.Document> documents) throws Exception {
		List<Document> docs= new ArrayList<Document>();
		for(org.apache.lucene.document.Document document:documents) {
		Document doc = EntityDocumentBuilder.convert(document);
		docs.add(doc);
		}
		this.addAllDocumentNoTest(docs);
	}
	
	
}
