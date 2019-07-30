package com.crs4.sem.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aeonbits.owner.ConfigFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.hibernate.cfg.Configuration;
import org.junit.Test;

import com.crs4.sem.config.SemEngineConfig;
import com.crs4.sem.exceptions.NotUniqueDocumentException;
import com.crs4.sem.model.Document;
import com.crs4.sem.neo4j.exceptions.CategoryNotFoundInTaxonomyException;
import com.crs4.sem.neo4j.exceptions.TaxonomyNotFoundException;
import com.crs4.sem.neo4j.service.TaxonomyService;
import com.crs4.sem.producers.DocumentProducer;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class DocumentServiceTest {
	
	//@Test
	public void testInsertRss() throws Exception{
		
		System.setProperty("sem.engine.basedirectory", "/Users/mariolocci/Documents/workspace-tmp/semengine_conf");
		SemEngineConfig config = ConfigFactory.create(SemEngineConfig.class,System.getProperties());
		File cfgFileh2= new File(config.getHibernateCFGDocuments());
		 Configuration configureh2 = HibernateConfigurationFactory.configureDocumentService(cfgFileh2);
		 DocumentService docservice = new DocumentService(configureh2);
		URL feedSource = new URL("http://www.repubblica.it/rss/economia/rss2.0.xml");
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
	} catch (NotUniqueDocumentException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	     }
	}
	
    @Test
    public void testAddDocument() throws Exception {
    	System.setProperty("sem.engine.basedire", "/Users/mariolocci/Documents/workspace-tmp/semengine_conf");
		DocumentService docservice= new DocumentService("jdbc:h2:mem:test","/Users/mariolocci/Documents/workspace-tmp/semengine_conf","org.hibernate.search.store.impl.RAMDirectoryProvider");
		ObjectMapper objectMapper = new ObjectMapper();
		//List<Doc> docs= gson.fromJson(new FileReader("src/test/resources/docs.json"), List.class);
		Document doc=objectMapper.readValue(new File("src/test/resources/doctoolong.json"), new TypeReference<Document>() { });
        docservice.addDocument(doc);
        assertNotNull(docservice.getById(45529L));
    }
    
    @Test
    public void testUpdateDocument() throws Exception {
    	System.setProperty("sem.engine.basedire", "/Users/mariolocci/Documents/workspace-tmp/semengine_conf");
		DocumentService docservice= new DocumentService("jdbc:h2:mem:test","/Users/mariolocci/Documents/workspace-tmp/semengine_conf","org.hibernate.search.store.impl.RAMDirectoryProvider");
		ObjectMapper objectMapper = new ObjectMapper();
		//List<Doc> docs= gson.fromJson(new FileReader("src/test/resources/docs.json"), List.class);
		Document doc=objectMapper.readValue(new File("src/test/resources/unique.json"), new TypeReference<Document>() { });
        docservice.addDocument(doc);
         doc=objectMapper.readValue(new File("src/test/resources/unique2.json"), new TypeReference<Document>() { });
       Long id=docservice.addDocument(doc);
        assertNotNull(docservice.getById(id));
       Document auxdoc=docservice.getById(id);
        assertEquals(auxdoc.getSource_id(),"16");
    }
    @Test
    public void testStreamDocument() throws Exception {
    	System.setProperty("sem.engine.basedire", "/Users/mariolocci/Documents/workspace-tmp/semengine_conf");
		DocumentService docservice= new DocumentService("jdbc:h2:mem:test","/Users/mariolocci/Documents/workspace-tmp/semengine_conf","org.hibernate.search.store.impl.RAMDirectoryProvider");
		InputStream input = new FileInputStream(new File("src/test/resources/docs.json"));
		int size=docservice.addDocuments(input);
         
        assertEquals(size,docservice.size(),0);
    }

    @Test
    public void testAddAllDocumentMovies() throws Exception {
    	System.setProperty("sem.engine.basedirectory", "/Users/mariolocci/Documents/workspace-tmp/semengine_conf");
		DocumentService docservice= new DocumentService("jdbc:h2:mem:test","/Users/mariolocci/Documents/workspace-tmp/semengine_conf","org.hibernate.search.store.impl.RAMDirectoryProvider");
		ObjectMapper objectMapper = new ObjectMapper();
		//List<Doc> docs= gson.fromJson(new FileReader("src/test/resources/docs.json"), List.class);
		List<Document> doc=objectMapper.readValue(new File("src/test/resources/test.json"), new TypeReference<List<Document>>() { });
        docservice.addAllDocument(doc);
        int size=doc.size();
        assertEquals(size,docservice.size(),0);
    }
    
  @Test
   public void iterateoverdocs() throws Exception {
		System.setProperty("sem.engine.basedirectory", "/Users/mariolocci/Documents/workspace-tmp/semengine_conf");
		DocumentService docservice= new DocumentService("jdbc:h2:mem:test","/Users/mariolocci/Documents/workspace-tmp/semengine_conf","org.hibernate.search.store.impl.RAMDirectoryProvider");
		ObjectMapper objectMapper = new ObjectMapper();
		InputStream input = new FileInputStream(new File("src/test/resources/docs.json"));
		int size=docservice.addDocuments(input);
		//docservice.setTrainableSomeSources(null);
  }
  
 @Test
 public void buildMass() throws IllegalArgumentException, FeedException, IOException, InterruptedException {
	 System.setProperty("sem.engine.basedirectory", "/Users/mariolocci/Documents/workspace-tmp/semengine_conf");
	 SemEngineConfig config = ConfigFactory.create(SemEngineConfig.class,System.getProperties());
	   File cfgFile=  new File(config.getHibernateCFGDocuments());
	    Configuration configure = HibernateConfigurationFactory.configureDocumentService(cfgFile);
	    DocumentService docservice= new DocumentService(configure);

	 docservice.rebuildIndex( );
 }
 
@Test
 public void migrationToMysql() throws IllegalArgumentException, FeedException, IOException, InterruptedException {
	 System.setProperty("sem.engine.basedirectory", "/Users/mariolocci/Documents/workspace-tmp/semengine_conf");

	 SemEngineConfig config = ConfigFactory.create(SemEngineConfig.class,System.getProperties());
	  File cfgFileh2= new File("src/test/resources/hibernate.h2.cfg.xml");
		 Configuration configureh2 = HibernateConfigurationFactory.configureDocumentService(cfgFileh2);
     DocumentService source= new DocumentService(configureh2);
     File cfgFile= new File("src/test/resources/hibernate.mysql.cfg.xml");
	 Configuration configure = HibernateConfigurationFactory.configureDocumentService(cfgFile);
	 DocumentService destination = new DocumentService(configure);
	 source.migrate(destination, false);
 }
 
 @Test
 public void testDetectKeywords() throws IOException {
	  Analyzer analyzer= new ShingleAnalyzerWrapper(2,3);
	  String text="May May adesso andrà in tour, in caso di bocciatura ha pronta la terza via";
	  String [] keywords= {"May","la terza via"};
	  Set<String> set=new HashSet<String>();
	  for(String k:keywords)
		  set.add(k.toLowerCase());
		  
	 String[] list = DocumentService.keywordsDetect(text, set, analyzer);
	 assertEquals(list.length,2);
 }
 
@Test
public void migrationToH2()  {
	 System.setProperty("sem.engine.basedire", "/Users/mariolocci/Documents/workspace-tmp/semengine_conf");

	 //SemEngineConfig config = ConfigFactory.create(SemEngineConfig.class);
	  File cfgFileh2= new File("src/test/resources/hibernate.h2new.cfg.xml");
		 Configuration configureh2 = HibernateConfigurationFactory.configureDocumentService(cfgFileh2);
  
   File cfgFileMysql= new File("src/test/resources/hibernate.mysql.cfg.xml");
	 Configuration configureMysql = HibernateConfigurationFactory.configureDocumentService(cfgFileMysql);
	 DocumentService source= new DocumentService(configureMysql);
	 DocumentService destination = new DocumentService(configureh2);
	 source.migrate(destination, false);
}




@Test
public void testbuildauthors()  {
	 System.setProperty("sem.engine.basedirectory", "/Users/mariolocci/Documents/workspace-tmp/semengine_conf");
	 SemEngineConfig config = ConfigFactory.create(SemEngineConfig.class,System.getProperties());
	  File cfgFile=  new File(config.getHibernateCFGDocuments());
	    Configuration configure = HibernateConfigurationFactory.configureDocumentService(cfgFile);
		DocumentService docservice= new DocumentService(configure);
		File cfgFile2= new File(config.getHibernateCFGAuthors());
		Configuration configuration2=HibernateConfigurationFactory .configureAuthorService(cfgFile2);
	    AuthorService authorService= new AuthorService(configuration2);	
	    
	
		//docservice.rebuildIndex();
		
		    
	 docservice.buildAuthors(authorService);
}
 
@Test
public void testDetectAllKeywords() throws TaxonomyNotFoundException, CategoryNotFoundInTaxonomyException  {
	 System.setProperty("sem.engine.basedirectory", "/Users/mariolocci/Documents/workspace-tmp/semengine_conf");
	 SemEngineConfig config = ConfigFactory.create(SemEngineConfig.class,System.getProperties());
	  File cfgFile=  new File(config.getHibernateCFGDocuments());
	    Configuration configure = HibernateConfigurationFactory.configureDocumentService(cfgFile);
		DocumentService docservice= new DocumentService(configure);
		TaxonomyService taxonomyService = new TaxonomyService( new File(config.neo4jDirectory()));
		Set<String> set = taxonomyService.getAllKeywords("root", false);
		Long value=docservice.detectKeywords(set);
}


}
