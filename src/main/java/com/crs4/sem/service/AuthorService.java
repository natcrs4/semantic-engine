package com.crs4.sem.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.CharsRefBuilder;
import org.apache.lucene.util.Version;
import org.hibernate.IdentifierLoadAccess;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.neo4j.graphalgo.impl.PageRankResult;

import com.crs4.sem.exceptions.NotUniqueDocumentException;
import com.crs4.sem.lucene.similarity.EntityRankScoreQuery;
import com.crs4.sem.lucene.similarity.FrequencyRankScoreQuery;
import com.crs4.sem.model.Author;
import com.crs4.sem.model.Document;
import com.crs4.sem.model.NewDocument;
import com.crs4.sem.model.SearchResult;

import lombok.Data;

@Data
public class AuthorService extends HibernateService {
	
	public static AuthorService instance;
	
	public static AuthorService newInstance(Configuration configuration) {
		if(instance==null)
			instance=new AuthorService(configuration);
		return instance;
	}

	public AuthorService(Configuration configuration) {
		super(configuration);
	};

	public List<String> getAuthors(String query) throws IOException {

		Session session = factory.openSession();
		FullTextSession fts = Search.getFullTextSession(session);
		Transaction tx = fts.beginTransaction();
		List<String> result = new ArrayList<String>();
		IndexReader indexReader = fts.getSearchFactory().getIndexReaderAccessor().open(Author.class);
		TermsEnum termsenum;
		try {
			termsenum = MultiFields.getTerms(indexReader, "authors_").iterator();

			CharsRefBuilder spare = new CharsRefBuilder();
			BytesRef term;

			while ((term = termsenum.next()) != null) {
				spare.copyUTF8Bytes(term);
				String text = spare.toString();
				result.add(text);
			}
			tx.commit();
		} catch (IOException e) {
			if (tx != null)
				tx.rollback();
			throw e;
		} finally {
			session.close();
		}
		return result;
	}
	private Author getAuthorId(String id, Session session) throws NotUniqueDocumentException {
		FullTextSession fts = Search.getFullTextSession(session);
		IdentifierLoadAccess<Author> search = fts.byId(Author.class);
		Author author = search.load(id);
		return author;
	}
	
	
	private Author addAuthor(Author author, Session session)  {
				    session.save(author);
         return author;

	}
	
	public void addAllAuthors(Collection<Author> collection) throws Exception {
		Session session = factory.openSession();
		Transaction tx = session.beginTransaction();
		try {
			for (Author doc : collection)
                  this.addAuthor(doc, session);

			tx.commit();
		} catch (RuntimeException e) {
			if (tx != null)
				tx.rollback();
			throw e; // or display error message
		} finally {
			session.close();
		}

	}
	
	public void updateFrequencyAllAuthors(Collection<Author> collection) throws Exception {
		Session session = factory.openSession();
		Transaction tx = session.beginTransaction();
		try {
			for (Author doc : collection) {
				
			     Author current = this.getAuthorId(doc.getId(), session);
			     if(current!=null) 
			    	 {current.setFrequency(current.getFrequency()+doc.getFrequency());
                      session.update(current);
			    	 }
			     else 
			    	 session.save(doc);
                  
			}
			tx.commit();
		} catch (RuntimeException e) {
			if (tx != null)
				tx.rollback();
			throw e; // or display error message
		} finally {
			session.close();
		}

	}
	
	public AuthorService() {
		super();
	}
	

	public List<Author> search(String query, Analyzer analyzer, int start, int maxresults) throws ParseException {
		Integer totaldocs = 0;
		Session session = factory.openSession();
		FullTextSession fts = Search.getFullTextSession(session);
		Transaction tx = fts.beginTransaction();

		QueryBuilder qb = fts.getSearchFactory().buildQueryBuilder().forEntity(Author.class).get();
		//org.apache.lucene.search.Query luceneQuery = qb.keyword().onFields("authors")
				//.matching(text).createQuery();
		Query luceneQuery = new QueryParser( "authors", analyzer).parse(query);
		 FrequencyRankScoreQuery customQuery = new FrequencyRankScoreQuery(luceneQuery);
		FullTextQuery fullTextQuery = fts.createFullTextQuery(customQuery, Author.class);

		fullTextQuery.setFirstResult(start);
		fullTextQuery.setMaxResults(maxresults);

		// execute search
		List<Author> result = fullTextQuery.list();
		totaldocs = fullTextQuery.getResultSize();
		tx.commit();
		session.close();
		
		return result;
	}

}
