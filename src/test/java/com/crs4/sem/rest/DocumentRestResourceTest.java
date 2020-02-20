package com.crs4.sem.rest;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Analyzer;
import com.crs4.sem.utils.Resolution;
import org.hibernate.cfg.Configuration;
import org.junit.Test;

import com.crs4.sem.model.NewSearchResult;
import com.crs4.sem.producers.AnalyzerProducer;
import com.crs4.sem.service.HibernateConfigurationFactory;
import com.crs4.sem.service.NewDocumentService;
import com.crs4.sem.service.ShadoService;

public class DocumentRestResourceTest {
	
	
	
	@Test
	public void testAdvancedSearch() throws Exception {
		
		DocumentRestResources rest = new DocumentRestResources();
		
		File cfgFile = new File("configurations/locale/hibernate.lucene.cfg2.xml");
		Configuration configure = HibernateConfigurationFactory.configureDocumentService(cfgFile);
		NewDocumentService documentService = new NewDocumentService(configure);
		
		AnalyzerProducer analyzerprod = new AnalyzerProducer();
		Analyzer analyzer = analyzerprod.produces();
		
		
		File cfgshado= new File("configurations/locale/hibernate.mysql.shado.cfg.xml");
		Configuration conf = HibernateConfigurationFactory.configureShadoService(cfgshado);
		ShadoService shadoService = new ShadoService(conf);
		rest.setLog(Logger.getLogger(DocumentRestResources.class.getName()));
		rest.setAnalyzer(analyzer);
		rest.setDocumentService(documentService);
		rest.setShadoService(shadoService);
		String text="rifiuti tossici ";
		String authors="";
		String categories="";
		String from=null;
		String to=null;
		boolean score=false;
		boolean histograms=true;
		Integer samplesize=200;
		boolean detect=false;
		double threshold=2.9;
		Integer maxresults=200;
		NewSearchResult searchResult = rest.advancedsearch(text, "", authors, "", categories, "", 0, maxresults, from, to, score, histograms, samplesize, detect, Resolution.DAY, false, threshold, false, false, false);
		assertEquals(searchResult.getDocuments().size()+searchResult.getDuplicated().size(),200,0.1);
		
	}

}
