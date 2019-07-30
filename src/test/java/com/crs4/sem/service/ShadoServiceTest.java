package com.crs4.sem.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.aeonbits.owner.ConfigFactory;
import org.hibernate.cfg.Configuration;
import org.junit.Test;

import com.crs4.sem.config.SemEngineConfig;
import com.crs4.sem.model.Document;
import com.crs4.sem.model.Documentable;
import com.crs4.sem.model.Link;
import com.crs4.sem.model.Shado;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mfl.sem.text.model.Doc;

public class ShadoServiceTest {
	
	@Test
	public void testSetUp() throws IOException {
		 System.setProperty("sem.engine.basedirectory", "/Users/mariolocci/Documents/workspace-tmp/semengine_conf");
		 SemEngineConfig config = ConfigFactory.create(SemEngineConfig.class);
		
			File cfgFile= new File(config.getHibernateCFGShado());
			Configuration configuration2=HibernateConfigurationFactory.configureShadoService(cfgFile);
		    ShadoService shadoService= new ShadoService(configuration2);	
		  
		    assertNotNull(shadoService);
	}
	
	@Test
	public void testadd()  {
		 System.setProperty("sem.engine.basedirectory", "/Users/mariolocci/Documents/workspace-tmp/semengine_conf");
		 SemEngineConfig config = ConfigFactory.create(SemEngineConfig.class,System.getProperties());
		
			File cfgFile= new File(config.getHibernateCFGShado());
			Configuration configuration2=HibernateConfigurationFactory .configureShadoService(cfgFile);
		    ShadoService shadoService= new ShadoService(configuration2);
		    ArrayList<Link> links = new ArrayList<Link>();
		    links.add(Link.builder().newLink("C").link("B").build());
		    links.add(Link.builder().newLink("C").link("F").build());
		    Shado shado = Shado.builder().internal_id("xxxxtrt").links(links).build();
		    
		
		   ArrayList<Shado> shadows = new ArrayList<Shado>();
		   shadows.add(shado);
		    shado = Shado.builder().internal_id("fdgdfhgfg").links(links).build();
		    
			
		   
		 
		    
			
		   
		   shadows.add(shado);
		  
		    HashSet<Shado> shadoset = new HashSet<Shado>();
		  shadoset.addAll(shadows);
		  shadoService.addAll(shadoset);
		    Shado s = shadoService.getShado("fdgdfhgfg");
		    assertEquals(s.getLinks().size(),2);
		    
	}

	@Test
	public void addJsonlinks() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
	  
	 	List<Document> docs=objectMapper.readValue(new File("src/test/resources/error400.json"), new TypeReference<List<Doc>>() { });
		
	}
}
