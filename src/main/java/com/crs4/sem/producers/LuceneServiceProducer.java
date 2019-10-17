package com.crs4.sem.producers;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.lucene.analysis.Analyzer;

import com.crs4.sem.config.SemEngineConfig;
import com.crs4.sem.service.LuceneService;

import lombok.Data;


@Data
public class LuceneServiceProducer {

	 @Inject
	 private SemEngineConfig config;
	 
	@Inject
	@AnalyzerType(Analyzers.ITALIAN) 
	private Analyzer analyzer;
		
	@Produces
	@DocumentProducerType(ServiceType.LUCENESERVICE)
    public LuceneService producer(InjectionPoint ip){
	   String source=config.indexbase()+"/com.crs4.sem.model.NewDocument";
		LuceneService luceneService=null;
		try {
			luceneService =  LuceneService.newInstance(source);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return luceneService;
	}
	
}
