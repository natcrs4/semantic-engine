package com.crs4.sem.service;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.List;

import org.aeonbits.owner.ConfigFactory;
import org.junit.Test;

import com.crs4.sem.config.SemEngineConfig;
import com.crs4.sem.exceptions.NotUniqueDocumentException;
import com.crs4.sem.model.Document;
import com.crs4.sem.model.SearchResult;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import org.neo4j.graphalgo.api.Graph;
import org.neo4j.graphalgo.core.GraphLoader;
import org.neo4j.graphalgo.core.huge.HugeGraphFactory;
import org.neo4j.graphalgo.impl.PageRankAlgorithm;
import org.neo4j.graphalgo.impl.PageRankResult;
import org.neo4j.graphdb.Direction;
import org.neo4j.kernel.internal.GraphDatabaseAPI;

public class SemanticEngineTest {
	
@Test	
	public void testPageRank() throws Exception {
			
			String taxo[]={"italia/politica","esteri","economia","societa/mare","tecnologia"};
			SemEngineConfig config = ConfigFactory.create(SemEngineConfig.class,System.getProperties());
			SemanticEngineService docservice= new SemanticEngineService(config);
			for (String cat:taxo){
			URL feedSource = new URL("http://www.lastampa.it/"+cat+"/rss.xml");
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		     }
		}
			
			Graph graph = new GraphLoader((GraphDatabaseAPI)docservice.getNodeService().getGraphDb())
	                .withDirection(Direction.BOTH)
	                .load(HugeGraphFactory.class);

			PageRankResult result = PageRankAlgorithm.of(graph, 0.85)
	        .compute(3)
	        .result();
System.out.println(result);
		}
		
	

@Test	
public void testSemanticSearchPageRank() throws Exception{
	{
		
		String taxo[]={"italia/politica/rss.xml","esteri/rss.xml","economia/rss.xml","societa/mare/rss.xml","tecnologia/rss.xml","/rss/lazampa"};
		SemEngineConfig config = ConfigFactory.create(SemEngineConfig.class,System.getProperties());
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
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	     }
	}
		
		 docservice.pageRank();
		 SearchResult searchResult=docservice.semanticSearch("gatto", 0, 1000);
		 SearchResult searchResult2=docservice.getDocumentService().searchDocument("gatto", 0, 1000, null,null);
		 assertNotNull(searchResult.getDocuments());
	}
	
}

}
