package com.crs4.sem.service;

import static org.junit.Assert.assertEquals;

import org.apache.lucene.analysis.Analyzer;
import org.junit.Test;

import com.crs4.sem.model.NewSearchResult;
import com.crs4.sem.producers.AnalyzerProducer;

public class LuceneServiceTest {
	
	
	@Test
	public void testSearch() throws Exception {
		String source="/Users/mariolocci/lucenedocs4/com.crs4.sem.model.NewDocument";
		LuceneService luceneService= new LuceneService(source);
		AnalyzerProducer producer = new AnalyzerProducer();
		Analyzer analyzer =producer.produces();
		NewSearchResult result = luceneService.parseSearch("casa","", null, null, 0, 10,false, analyzer, false);
		assertEquals(result.getTotaldocs(),1255,0.1);
	}

}
