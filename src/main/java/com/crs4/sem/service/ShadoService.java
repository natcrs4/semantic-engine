package com.crs4.sem.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.IdentifierLoadAccess;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Projections;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.stat.Statistics;

import com.crs4.sem.model.Document;
import com.crs4.sem.model.Link;
import com.crs4.sem.model.Metadata;
import com.crs4.sem.model.NewDocument;
import com.crs4.sem.model.NewMetadata;
import com.crs4.sem.model.Page;
import com.crs4.sem.model.Shado;

import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
public class ShadoService extends HibernateService{

	
	public static final Logger logger = Logger.getLogger(ShadoService.class);
	public static ShadoService instance;
	public static ShadoService newInstance(Configuration configuration) {
		if(instance==null)
			instance=new ShadoService(configuration);
		return instance;
	}
	
	public ShadoService(Configuration configure) {
		super(configure);
	}



	public void addAll(Set<Shado> shadows)  {
		Session session = factory.openSession();
		Transaction tx = session.beginTransaction();
		try {
			for (Shado shado : shadows) {
                
				 session.saveOrUpdate(shado);
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
	
	public Shado getShado(String id) {
		
		Session session = factory.openSession();
		Transaction tx = session.beginTransaction();
		Shado shado=null;
		try {
		
		
	    shado = session.get(Shado.class, id);
	   // Hibernate.initialize(shado);
	    tx.commit();
	   
	} catch (RuntimeException e){
		if (tx != null)
			tx.rollback();
		throw e; // or display error message
	}

	finally {
		session.close();
	}
		 return shado;
	}

    public void setMD5id(List<Shado> shadows) {
    	for(Shado shado:shadows)
    	for( Link link:shado.getLinks()) {
    	     String md5 = DigestUtils.md5Hex(link.getLink());
    	     link.setId(md5);
    	}
    }

	public Link getLink(String id) {
		Session session = factory.openSession();
		Transaction tx = session.beginTransaction();
		Link link=null;
		try {
	     link = session.get(Link.class, id);
	   // Hibernate.initialize(shado);
	    tx.commit();
	   
		} catch (RuntimeException e){
			if (tx != null)
				tx.rollback();
			throw e; // or display error message
		}

		finally {
			session.close();
		}
		return link;
		
		
	}

	public Set<NewMetadata> getMetadata( Set<NewMetadata> links) {		
		Set<NewMetadata> metadatas= new HashSet<NewMetadata>();
		for(NewMetadata link:links) {
			 String md5 = DigestUtils.md5Hex(link.getUrl());
			 Link exlink = this.getLink(md5);
			 if(exlink!=null) { 
			 Metadata current = exlink.getMetadata();
			 current.setUrl(exlink.getNewLink());
			 NewMetadata newmetadata=toNewMetadata(current);
			 newmetadata.setId(md5);
			 newmetadata.setUrl(link.getUrl());
			 metadatas.add(newmetadata);
			 }
			
				 
		}
		return metadatas;
	}
	public Set<Page> getPage(Set<Page> links) {
		Set<Page> metadatas= new HashSet<Page>();
		for(Page link:links) {
			 String md5 = DigestUtils.md5Hex(link.getUrl());
			 Link exlink = this.getLink(md5);
			 if(exlink!=null) { 
			 Metadata current = exlink.getMetadata();
			 current.setUrl(exlink.getNewLink());
			 Page newpage=toPage(current);
			 newpage.setId(md5);
			 newpage.setUrl(link.getUrl());
			 metadatas.add(newpage);
			 }
			
				 
		}
		return metadatas;
	}

	private Page toPage(Metadata current) {
		return Page.builder().newurl(current.getUrl()).build();
	
	}



	private NewMetadata toNewMetadata(Metadata current) {
		return NewMetadata.builder().description(current.getDescription()).duration(current.getDuration()).newurl(current.getUrl()).poster(current.getPoster()).title(current.getTitle()).build();
		//return null;
	}



	public List<Shado> checkLinks(List<Shado> shadows) {
	Map<String,Link> map = new HashMap<String,Link>();
	List<Shado> rs= new ArrayList<Shado>();
 	    for(Shado shado:shadows) {
	    	List<Link> list= new ArrayList<Link>();
	    	for(Link link:shado.getLinks())
	    	{ Link el=map.get(link.getId());
	    	  if(el!=null)
	    		  list.add(el);
	    	  else {
	    		   map.put(link.getId(), link);
	    		   list.add(link);
	    	  }
	    	}
	    	shado.setLinks(list);
	    	rs.add(shado);
	    }
	    	
		return rs;
	}



	public Long size() {
		
		Session session = factory.openSession();
		FullTextSession fts = Search.getFullTextSession(session);
		Transaction tx = fts.beginTransaction();
		Criteria criteriaCount = session.createCriteria(Shado.class);
		criteriaCount.setProjection(Projections.rowCount());
		Long count = (Long) criteriaCount.uniqueResult();
		tx.commit();
		session.close();
		return count;
	}



	



	public Shado deleteDocument(String id) {
		Session session = factory.openSession();
		Transaction tx = session.beginTransaction();
		Shado shado=null;
		try {
	    shado = session.get(Shado.class, id);
	   // Hibernate.initialize(shado);
	    if(shado!=null)
	    	session.delete(shado);
	    tx.commit();
		} catch (RuntimeException e){
			if (tx != null)
				tx.rollback();
			throw e; // or display error message
		}

		finally {
			session.close();
		}
			
		
	
		return shado;
	}



	public void migrate(ShadoService destination) {
Session session = factory.openSession();
		
	
		session.setFlushMode(FlushMode.MANUAL);
		session.setCacheMode(CacheMode.IGNORE);
		Transaction transaction = session.beginTransaction();
		ScrollableResults scroll = session.createCriteria(Shado.class).setFetchSize(BATCH_SIZE).scroll(ScrollMode.FORWARD_ONLY);
	
		Set<Shado> queue= new HashSet<Shado>();
		int index=0;
		logger.info("starting migrate");
		while(scroll.next()) {
			Shado shado = (Shado)scroll.get(0);
			queue.add(shado);
			
			if (index % BATCH_SIZE == 0) {
				destination.addAll(queue);
				session.flush();
				session.clear();
				queue= new HashSet<Shado>();
				logger.info("added " + queue. size() + " shado");
			}
			index++;
			}
		
		transaction.commit();
		
		session.close();
		logger.info("migration terminated");
	}



	
}
