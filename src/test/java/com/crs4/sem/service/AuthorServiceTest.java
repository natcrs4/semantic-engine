package com.crs4.sem.service;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.aeonbits.owner.ConfigFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.hibernate.cfg.Configuration;
import org.junit.Test;

import com.crs4.sem.config.SemEngineConfig;
import com.crs4.sem.model.Author;

public class AuthorServiceTest {
	
	
	@Test
	public void testGetAuthors() throws IOException {
		 System.setProperty("sem.engine.basedirectory", "/Users/mariolocci/Documents/workspace-tmp/semengine_conf");
		 SemEngineConfig config = ConfigFactory.create(SemEngineConfig.class,System.getProperties());
		
			File cfgFile2= new File(config.getHibernateCFGAuthors());
			Configuration configuration2=HibernateConfigurationFactory .configureAuthorService(cfgFile2);
		    AuthorService authorService= new AuthorService(configuration2);	
		    List<String> list = authorService.getAuthors("");
		    assertTrue(list.size()>1000);
	}

	
	@Test
	public void testSearch() throws IOException, ParseException {
		 System.setProperty("sem.engine.basedirectory", "/Users/mariolocci/Documents/workspace-tmp/semengine_conf");
		 SemEngineConfig config = ConfigFactory.create(SemEngineConfig.class,System.getProperties());
		
			File cfgFile2= new File(config.getHibernateCFGAuthors());
			Configuration configuration2=HibernateConfigurationFactory .configureAuthorService(cfgFile2);
		    AuthorService authorService= new AuthorService(configuration2);	
		    List<Author> list = authorService.search("casa", new StandardAnalyzer(), 1,10);
		    assertTrue(list.size()>1000);
	}
}
