package com.crs4.sem.producers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.cfg.Configuration;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import com.crs4.sem.config.SemEngineConfig;
import com.crs4.sem.model.NewDocument;
import com.crs4.sem.neo4j.service.NodeService;
import com.crs4.sem.service.HibernateConfigurationFactory;
import com.crs4.sem.service.NERService;
import com.crs4.sem.service.NewDocumentService;
import com.crs4.sem.service.SemanticEngineService;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class SemanticEngineProducer {
	
	 @Inject
	  private SemEngineConfig config;
	 @Inject
	  private NERService nerservice;
	
	 @Produces
	 public SemanticEngineService produces() throws IOException {
		 String path=config.getHibernateSemDocuments();
		    if (path.startsWith("classpath:")) {
		    	path=path.replace("classpath:", config.classpath()+"/applications/"+config.applicationame()+"/WEB-INF/classes/");
		    }

		 Configuration configure = HibernateConfigurationFactory.configureDocumentService(new File(path));
			NewDocumentService ndocservice= new NewDocumentService(configure);
		
		
		GraphDatabaseService graph1 = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File(config.semneo4j()))
		.setConfig(GraphDatabaseSettings.pagecache_memory, "512M")
		.setConfig(GraphDatabaseSettings.string_block_size, "60")
		.setConfig(GraphDatabaseSettings.array_block_size, "300")
		.setConfig(GraphDatabaseSettings.read_only,"false").newGraphDatabase();
		
		SemanticEngineService docservice= new SemanticEngineService(new NodeService(graph1), nerservice, ndocservice);
		String taxo[]={"esteri","economia","societa/mare","tecnologia"};
		long id=0L;
		for (String cat:taxo){
			try {
		URL feedSource = new URL("http://feed.lastampa.it/"+cat+".rss");
		SyndFeedInput input = new SyndFeedInput();
		SyndFeed feed;
	
			feed = input.build(new XmlReader(feedSource));
			String desc=feed.getDescription();
			List<SyndEntry> entries = (List<SyndEntry>)feed.getEntries();
		    for (SyndEntry entry: entries){
		    	  System.out.println("Title: " + entry.getTitle());
	        System.out.println("Link: " + entry.getLink());
	        System.out.println("Author: " + entry.getAuthor());
	        System.out.println("Publish Date: " + entry.getPublishedDate());
	        System.out.println("Description: " + entry.getDescription().getValue());
	        id++;
	       NewDocument document=NewDocument.builder().internal_id(DigestUtils.md5Hex(entry.getLink())).id(id).authors(entry.getAuthor()).description(entry.getDescription().getValue()).url(entry.getLink()).title(entry.getTitle()).publishDate(entry.getPublishedDate()).build();
	       docservice.addDocument(document);
		    } }catch (IllegalArgumentException | FeedException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	   
	}
			
			docservice.pageRank();
			return docservice;
	 }

}
