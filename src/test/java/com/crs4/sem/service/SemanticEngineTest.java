package com.crs4.sem.service;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.List;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.cfg.Configuration;
import org.junit.Test;

import com.crs4.sem.config.SemEngineConfig;
import com.crs4.sem.exceptions.NotUniqueDocumentException;
import com.crs4.sem.model.Document;
import com.crs4.sem.model.NewDocument;
import com.crs4.sem.model.NewSearchResult;
import com.crs4.sem.model.SearchResult;
import com.crs4.sem.neo4j.service.NodeService;
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
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.kernel.internal.GraphDatabaseAPI;

public class SemanticEngineTest {
	
@Test	
	public void testPageRank() throws IOException {
			
		
			 Configuration configure = HibernateConfigurationFactory.configureDocumentService(new File("src/test/resources/hibernate.h2.sem.cfg.xml"));
				NewDocumentService ndocservice= new NewDocumentService(configure);
			
			File file = new File("/Users/mariolocci/Documents/workspace-tmp/semengine_conf/resources/icab.par");
			NERService nerservice = new NERService(file);
			GraphDatabaseService graph1 = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File("/Users/mariolocci/lucene_h2_sem/neo4j"))
			.setConfig(GraphDatabaseSettings.pagecache_memory, "512M")
			.setConfig(GraphDatabaseSettings.string_block_size, "60")
			.setConfig(GraphDatabaseSettings.array_block_size, "300")
			.setConfig(GraphDatabaseSettings.read_only,"false").newGraphDatabase();
			
			SemanticEngineService docservice= new SemanticEngineService(new NodeService(graph1), nerservice, ndocservice);
			String taxo[]={"italia/politica","esteri","economia","societa/mare","tecnologia"};
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
			PageRankResult result = docservice.getPageRankResult();
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
       NewDocument document=NewDocument.builder().authors(entry.getAuthor()).description(entry.getDescription().getValue()).title(entry.getTitle()).publishDate(entry.getPublishedDate()).build();
	   try {
		docservice.addDocument(document);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	     }
	}
		
		 docservice.pageRank();
		 NewSearchResult searchResult=docservice.semanticSearch("gatto", 0, 1000);
		 NewSearchResult searchResult2=docservice.getDocumentService().searchDocument("gatto", 0, 1000, null,null);
		 assertNotNull(searchResult.getDocuments());
	}
	
}

}
