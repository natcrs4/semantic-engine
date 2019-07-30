package com.crs4.sem.producers;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import com.crs4.sem.analysis.CommaAnalyzer;
import com.crs4.sem.analysis.ItalianClassifierAnalyzer;

@ApplicationScoped
public class AnalyzerProducer {
	

	
	@Produces
	@AnalyzerType(Analyzers.ITALIAN)
	public Analyzer producer(InjectionPoint ip){
		return this.produces();
	}
	
	public Analyzer produces(){
		Analyzer analyzer;
		Map<String, Analyzer> mapanalyzer=new HashMap<String,Analyzer>();
    	// { "id", "url", "title", "description", "authors", "type", "source_id", "internal_id",
			//	"publishDate", "links", "movies", "gallery", "attachments", "podcasts", "score", "neoid", "entities",
			//	"categories", "trainable" };
    	mapanalyzer.put("url", new StandardAnalyzer());
    	mapanalyzer.put("authors", new StandardAnalyzer());
    	mapanalyzer.put("links", new StandardAnalyzer());
    	mapanalyzer.put("gallery", new StandardAnalyzer());
    	mapanalyzer.put("movies", new StandardAnalyzer());
    	mapanalyzer.put("attachments", new StandardAnalyzer());
    	mapanalyzer.put("podcasts", new StandardAnalyzer());
    	mapanalyzer.put("type", new WhitespaceAnalyzer());
    	mapanalyzer.put("categories", new KeywordAnalyzer());
    	mapanalyzer.put("keywords", new KeywordAnalyzer());
		analyzer= new PerFieldAnalyzerWrapper(new ItalianClassifierAnalyzer(),mapanalyzer);
		
		return analyzer;
	}

}
