package com.crs4.sem.service;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.aeonbits.owner.ConfigFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.hibernate.cfg.Configuration;
import org.junit.Test;

import com.crs4.sem.config.SemEngineConfig;
import com.crs4.sem.model.Documentable;
import com.crs4.sem.model.NewDocument;
import com.crs4.sem.model.NewSearchResult;
import com.crs4.sem.rest.serializer.DateSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class NewDocumentServiceTest {
	@Test	
	public void changeIndexStructure() throws IOException{
		
		String source="/Users/mariolocci/Downloads/lucene_recover/com.crs4.sem.model.Document";
		File cfgFile = new File("src/test/resources/hibernate.lucene.cfg2.xml");
		Configuration configure = HibernateConfigurationFactory.configureDocumentService(cfgFile);
		NewDocumentService destination = new NewDocumentService(configure);
		Path path = Paths.get(source);
		Directory directory = FSDirectory.open(path);
	    IndexReader indexReader = DirectoryReader.open(directory);
	    int num = indexReader.numDocs()	;
	    System.out.println(" found index whith size "+num);
	    List<Document> documents= new ArrayList<Document>();
	    for(int i=0;i<num;i++) {
	    	Document document;
	     
			try {
				document = indexReader.document(i);
				if(document!=null) 
					documents.add(document);
				   if(i%200==0) { 
			     destination.addAllLuceneDocument(documents);
			     documents= new ArrayList<Document>();
			     System.out.println("added "+ i + " documents");
				   }
			} catch ( Exception e) {
				// TODO Auto-generated catch block
		      e.printStackTrace();
		      System.out.println("skipped "+ i + " documents");
		      
			}
	    }
	    		indexReader.close();
	    		directory.close();
	    		
	    }

@Test
public void searchText() {
	File cfgFile = new File("src/test/resources/hibernate.lucene.cfg2.xml");
	Configuration configure = HibernateConfigurationFactory.configureDocumentService(cfgFile);
	NewDocumentService destination = new NewDocumentService(configure);
	NewSearchResult serp = destination.searchDocument("casa", 0,10, null, null);
	assertTrue(serp.getTotaldocs()>1);
}
	
@Test
public void trainable() {
	File cfgFile = new File("src/test/resources/hibernate.lucene.cfg2.xml");
	Configuration configure = HibernateConfigurationFactory.configureDocumentService(cfgFile);
	NewDocumentService destination = new NewDocumentService(configure);
	 List<Documentable> serp = destination.getTrainable();
	assertTrue(serp.size()>1);
}

@Test
public void testbuildauthors()  {
	 System.setProperty("sem.engine.basedirectory", "/Users/mariolocci/Documents/workspace-tmp/semengine_conf");
	 SemEngineConfig config = ConfigFactory.create(SemEngineConfig.class,System.getProperties());
	  File cfgFile=  new File(config.getHibernateCFGDocuments());
	    Configuration configure = HibernateConfigurationFactory.configureDocumentService(cfgFile);
		NewDocumentService docservice= new NewDocumentService(configure);
		File cfgFile2= new File(config.getHibernateCFGAuthors());
		Configuration configuration2=HibernateConfigurationFactory .configureAuthorService(cfgFile2);
	    AuthorService authorService= new AuthorService(configuration2);	
	    
	
		//docservice.rebuildIndex();
		
		    
	 docservice.buildAuthors(authorService);
}


@Test
public void testAddDocuments() throws Exception {

			System.setProperty("sem.engine.basedirectory", "/Users/mariolocci/Documents/workspace-tmp/semengine_conf");
			NewDocumentService docservice= new NewDocumentService("jdbc:h2:mem:test","/Users/mariolocci/Documents/workspace-tmp/semengine_conf","org.hibernate.search.store.impl.RAMDirectoryProvider");
		
			InputStream input = new FileInputStream(new File("src/test/resources/newdocs.json"));
			int size=docservice.addDocuments(input);
			//docservice.setTrainableSomeSources(null);
			
	  }

@Test
public void testdeletedocument() throws Exception {

			System.setProperty("sem.engine.basedirectory", "/Users/mariolocci/Documents/workspace-tmp/semengine_conf");
			NewDocumentService docservice= new NewDocumentService("jdbc:h2:mem:test","/Users/mariolocci/Documents/workspace-tmp/semengine_conf","org.hibernate.search.store.impl.RAMDirectoryProvider");
		
			InputStream input = new FileInputStream(new File("src/test/resources/newdocs2.json"));
			int size=docservice.addDocuments(input);
			NewDocument result = docservice.getById("2d7060eb02ec1c1b6a7312b4e4f4df069d07e027",true);
			assertTrue(result!=null);
			docservice.deleteDocument("2d7060eb02ec1c1b6a7312b4e4f4df069d07e027");
		     result = docservice.getById("2d7060eb02ec1c1b6a7312b4e4f4df069d07e027",true);
			assertTrue(result==null);
	  }

@Test
public void testUploadNewTestJson() throws Exception {

			System.setProperty("sem.engine.basedirectory", "/Users/mariolocci/Documents/workspace-tmp/semengine_conf");
			NewDocumentService docservice= new NewDocumentService("jdbc:h2:mem:test","/Users/mariolocci/Documents/workspace-tmp/semengine_conf","org.hibernate.search.store.impl.RAMDirectoryProvider");
		
			InputStream input = new FileInputStream(new File("src/test/resources/newtest.json"));
			int size=docservice.addDocuments(input);
			//docservice.setTrainableSomeSources(null);
			input = new FileInputStream(new File("src/test/resources/newtest.json"));

		docservice.addDocuments(input);
			
	  }

@Test
public void testAddAllNewTestJson() throws Exception {

			System.setProperty("sem.engine.basedirectory", "/Users/mariolocci/Documents/workspace-tmp/semengine_conf");
			NewDocumentService docservice= new NewDocumentService("jdbc:h2:mem:test","/Users/mariolocci/Documents/workspace-tmp/semengine_conf","org.hibernate.search.store.impl.RAMDirectoryProvider");
		
			InputStream input = new FileInputStream(new File("src/test/resources/newtest.json"));
			int size=docservice.addDocuments(input);
			//docservice.setTrainableSomeSources(null);
			input = new FileInputStream(new File("src/test/resources/newtest.json"));

		docservice.addDocuments(input);
			
	  }
@Test
public void getNatualId() {
	File cfgFile = new File("src/test/resources/hibernate.lucene.cfg2.xml");
	Configuration configure = HibernateConfigurationFactory.configureDocumentService(cfgFile);
	NewDocumentService destination = new NewDocumentService(configure);
	NewDocument doc = destination.getById("2d7060eb02ec1c1b6a7312b4e4f4df069d07e027",true);
	NewDocument serp = destination.getNaturald(doc.getId(),true);
	assertTrue(serp!=null);
}

@Test
public void testRebuildIndex() throws InterruptedException {
	File cfgFile = new File("src/test/resources/hibernate.lucene.cfg2.xml");
	Configuration configure = HibernateConfigurationFactory.configureDocumentService(cfgFile);
	NewDocumentService destination = new NewDocumentService(configure);
	destination.rebuildIndex();
	assertTrue(destination.size()==destination.indexsize());
}


}
