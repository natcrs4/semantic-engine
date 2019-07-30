package com.crs4.sem.producers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.junit.Test;
import org.neo4j.io.fs.FileUtils;

import com.crs4.sem.analysis.ItalianClassifierAnalyzer;
import com.crs4.sem.model.Document;
import com.crs4.sem.neo4j.exceptions.CategoryNotFoundInTaxonomyException;
import com.crs4.sem.neo4j.service.TaxonomyCSVReader;
import com.crs4.sem.neo4j.service.TaxonomyService;
import com.crs4.sem.service.DocumentService;
import com.crs4.sem.service.NewDocumentService;
import com.mfl.sem.classifier.exception.ClassifierException;
import com.mfl.sem.classifier.text.TextClassifier;
import com.mfl.sem.model.ScoredItem;

public class ClassifierProducerTest {
	
	
	@Test
	public void producer() throws InstantiationException, IllegalAccessException, IOException, ClassifierException, CategoryNotFoundInTaxonomyException {
		ClassifierProducer producer = new ClassifierProducer();
		producer.setAnalyzer(new ItalianClassifierAnalyzer());
		TaxonomyService taxonomyService = this.buildTaxonomy();
		NewDocumentService docservice= new NewDocumentService("jdbc:h2:mem:test","/Users/mariolocci/Documents/workspace-tmp/semengine_conf","org.hibernate.search.store.impl.RAMDirectoryProvider");
		producer.setDocservice(docservice);
		producer.setTaxoservice(taxonomyService);
		producer.init();
		TextClassifier classifier = producer.producer();
		List<ScoredItem> classes = classifier.classify(Document.builder().title("biologico").build());
		assertEquals(classes.get(0).getLabel(),"cultura");
		 classes = classifier.classify(Document.builder().title("diritti umani").build());
		assertEquals(classes.get(0).getLabel(),"diritti umani");
	}
	
	
	private TaxonomyService buildTaxonomy() throws IOException, FileNotFoundException {
		File neodirectory = new File("/tmp/test");
		   if(neodirectory.exists())
			   FileUtils.deleteRecursively(neodirectory);;
		   TaxonomyService taxoservice = new TaxonomyService(new File("/tmp/test"));
		   TaxonomyCSVReader.readTriple(new FileInputStream(new File("src/test/resources/SOS_181202_Tassonomia_rev1.csv")), taxoservice);
		return taxoservice;
	}
}
