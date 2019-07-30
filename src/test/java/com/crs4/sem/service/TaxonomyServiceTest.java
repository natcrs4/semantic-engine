package com.crs4.sem.service;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.aeonbits.owner.ConfigFactory;
import org.junit.Test;
import org.neo4j.graphdb.Node;

import com.crs4.sem.config.SemEngineConfig;
import com.crs4.sem.neo4j.model.RRelationShipType;
import com.crs4.sem.neo4j.service.TaxonomyService;

public class TaxonomyServiceTest {
	
	
	//@Test
	public void testaddCategory(){
		SemEngineConfig config = ConfigFactory.create(SemEngineConfig.class,System.getProperties());
		TaxonomyService taxoservice = new TaxonomyService( new File(config.neo4jDirectory()));
		Node cat=null;
		if( taxoservice.searchCategory("Root")==null)
		    cat=taxoservice.createCategory("Root");
		
		Node node = taxoservice.searchCategory("Root");
		assertNotNull(node);
	}
	
   

}
