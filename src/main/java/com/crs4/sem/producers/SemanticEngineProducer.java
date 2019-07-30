package com.crs4.sem.producers;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.aeonbits.owner.ConfigFactory;

import com.crs4.sem.config.SemEngineConfig;
import com.crs4.sem.exceptions.NotUniqueDocumentException;
import com.crs4.sem.model.Document; 
import com.crs4.sem.model.Documentable;
import com.crs4.sem.service.SemanticEngineService;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class SemanticEngineProducer {
	
	 @Inject
	  private SemEngineConfig config;
	
	 @Produces
	 public SemanticEngineService produces() throws IOException, IllegalArgumentException, FeedException, NotUniqueDocumentException{
		 String taxo[]={"italia/politica/rss.xml","esteri/rss.xml","economia/rss.xml","societa/mare/rss.xml","tecnologia/rss.xml","/rss/lazampa"};
			SemEngineConfig config = ConfigFactory.create(SemEngineConfig.class);
			SemanticEngineService docservice= new SemanticEngineService(config);
			for (String cat:taxo){
			URL feedSource = new URL("http://www.lastampa.it/"+cat);
			SyndFeedInput input = new SyndFeedInput();
			SyndFeed feed = input.build(new XmlReader(feedSource));
			String desc=feed.getDescription();
			List<SyndEntry> entries = (List<SyndEntry>)feed.getEntries();
		    for (SyndEntry entry: entries){
		    	  System.out.println("Title: " + entry.getTitle());
	        System.out.println("Link: " + entry.getLink());
	        System.out.println("Author: " + entry.getAuthor());
	        System.out.println("Publish Date: " + entry.getPublishedDate());
	        System.out.println("Description: " + entry.getDescription().getValue());
	       Document document=Document.builder().authors(entry.getAuthor()).description(entry.getDescription().getValue()).title(entry.getTitle()).publishDate(entry.getPublishedDate()).build();
		   try {
			docservice.addDocument(document);
		} catch ( Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		     }
		}
			
			docservice.pageRank();
			return docservice;
	 }

}
