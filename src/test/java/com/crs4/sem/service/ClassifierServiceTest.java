package com.crs4.sem.service;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aeonbits.owner.ConfigFactory;
import org.apache.lucene.analysis.Analyzer;
import org.hibernate.cfg.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.io.fs.FileUtils;

import com.crs4.sem.analysis.ItalianClassifierAnalyzer;
import com.crs4.sem.config.SemEngineConfig;
import com.crs4.sem.exceptions.NotUniqueDocumentException;
import com.crs4.sem.model.Document;
import com.crs4.sem.model.Documentable;
import com.crs4.sem.model.NewDocument;
import com.crs4.sem.neo4j.exceptions.CategoryNotFoundInTaxonomyException;
import com.crs4.sem.neo4j.service.TaxonomyCSVReader;
import com.crs4.sem.neo4j.service.TaxonomyService;
import com.crs4.sem.producers.SourceProducer;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.mfl.sem.classifier.HClassifier;
import com.mfl.sem.classifier.HClassifierBuilder;
import com.mfl.sem.classifier.exception.ClassifierException;
import com.mfl.sem.classifier.impl.SVMClassifier;
import com.mfl.sem.classifier.model.CategoryDictionary;
import com.mfl.sem.classifier.performance.Precision;
import com.mfl.sem.classifier.text.Documents;
import com.mfl.sem.classifier.text.TextClassifier;
import com.mfl.sem.classifier.text.impl.TextClassifierImpl;
import com.mfl.sem.dataset.reader.DocumentReader;
import com.mfl.sem.model.ScoredItem;

import lombok.Data;

@Data
public class ClassifierServiceTest {
	
	private TextClassifier textClassifier;
	private TaxonomyService taxonomyService;
	private DocumentService documentService;
	private Analyzer analyzer;
	public void produceDocumentService() throws Exception {
		 
		    	System.setProperty("sem.engine.basedire", "/Users/mariolocci/Documents/workspace-tmp/semengine_conf");
				DocumentService docservice= new DocumentService("jdbc:h2:mem:test","/Users/mariolocci/Documents/workspace-tmp/semengine_conf","org.hibernate.search.store.impl.RAMDirectoryProvider");
				InputStream input = new FileInputStream(new File("src/test/resources/docs.json"));
				int size=docservice.addDocuments(input);
		         this.documentService=docservice;
		        
		    }

	public void produceTaxonomy() throws IOException {
	
			File neodirectory = new File("/tmp/test");
			   if(neodirectory.exists())
				   FileUtils.deleteRecursively(neodirectory);;
			   TaxonomyService taxoservice = new TaxonomyService(new File("/tmp/test"));
			   TaxonomyCSVReader.readTriple(new FileInputStream(new File("src/test/resources/SOS_181202_Tassonomia_rev1.csv")), taxoservice);
			this.taxonomyService= taxoservice;
		
	}

	public void produceClassifier() throws Exception {
		this.produceDocumentService();
		this.produceTaxonomy();
		this.analyzer=new ItalianClassifierAnalyzer();
		{
			CategoryDictionary categoryDictionary = new CategoryDictionary();
			Node root = taxonomyService.searchCategory("root");
			HClassifier<SVMClassifier> hclassifier;
			try {
				hclassifier = HClassifierBuilder.builder().species(SVMClassifier.class).root(root)
						.taxonomyService(taxonomyService).categoryBuilder(categoryDictionary).build();
			
			TextClassifier textClassifier = new TextClassifierImpl(analyzer, hclassifier,categoryDictionary);
			List<Documentable> docs= new ArrayList<Documentable>();
			List<Documentable> docs_ = documentService.getTrainable();
			String[] categories = taxonomyService.branchLabels(root, false);
			List<Documentable> kdocs = new ArrayList<Documentable>();
			
			
			for (String category : categories) {
				String[] keywords;
				keywords = taxonomyService.getKetwords("root", category);
				//if(category.trim().length()==0) System.out.println("_____");
				String[] categories_ = new String[1];
				categories_[0] = category;
				if(keywords!=null) 
				for (int i = 0; i < keywords.length; i++) {
					Documentable doc = Document.builder().title(keywords[i]).categories(categories_).build();
	                kdocs.add(doc);
				}
			}
			docs.addAll(kdocs);
			if(!docs_.isEmpty())docs.addAll(docs_);
			Documents kdocsreader = new DocumentReader(docs);
			textClassifier.train(kdocsreader);
			this.setTextClassifier(textClassifier);
			} catch (InstantiationException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassifierException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CategoryNotFoundInTaxonomyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
	@Test
	public void classifyDocumentsTest() throws Exception {
		produceClassifier();
		Long i=documentService.classifyAll(this.textClassifier);
		assertEquals(i,10,0);
	}

	@Test
	public void buildClassifierfromSources() throws IOException, InstantiationException, IllegalAccessException, CategoryNotFoundInTaxonomyException, ClassifierException {
		System.setProperty("sem.engine.basedirectory", "/Users/mariolocci/Documents/workspace-tmp/semengine_conf");
		SemEngineConfig config = ConfigFactory.create(SemEngineConfig.class,System.getProperties());
		
		File cfgFileh2= new File("src/test/resources/hibernate.lucene.cfg2.xml");
		 Configuration configureh2 = HibernateConfigurationFactory.configureDocumentService(cfgFileh2);
		 NewDocumentService docservice = new NewDocumentService(configureh2);
     
		this.produceTaxonomy();
		this.analyzer=new ItalianClassifierAnalyzer();
		SourceProducer sourceproducer = new SourceProducer();
	sourceproducer.setConfig(config);
		Map<String, String> sourceidcat = sourceproducer.produces();
		CategoryDictionary categoryDictionary = new CategoryDictionary();
		Node root = taxonomyService.searchCategory("root");
		HClassifier<SVMClassifier> hclassifier;
		
			hclassifier = HClassifierBuilder.builder().species(SVMClassifier.class).root(root)
					.taxonomyService(taxonomyService).categoryBuilder(categoryDictionary).build();
		
		TextClassifier textClassifier = new TextClassifierImpl(analyzer, hclassifier,categoryDictionary);
		List<Documentable> docs= new ArrayList<Documentable>();
		//List<Documentable> docs_ = docservice.trainsetUsingSource(sourceidcat,200);
	
		String[] categories = taxonomyService.branchLabels(root, false);
		List<Documentable> kdocs = new ArrayList<Documentable>();
		
		
		for (String category : categories) {
			String[] keywords;
			keywords = taxonomyService.getKetwords("root", category);
			//if(category.trim().length()==0) System.out.println("_____");
			String[] categories_ = new String[1];
			categories_[0] = category;
			if(keywords!=null) 
			for (int i = 0; i < keywords.length; i++) {
				Documentable doc = NewDocument.builder().title(keywords[i]).categories(categories_).build();
                kdocs.add(doc);
			}
		}
		docs.addAll(kdocs);
		//if(!docs_.isEmpty())docs.addAll(docs_);
		Documents kdocsreader = new DocumentReader(docs);
		textClassifier.train(kdocsreader);
		this.setTextClassifier(textClassifier);
		NewDocument doc= NewDocument.builder().description("Milano ha tenuto l’aliquota base (0,4%). Roma l’ha portata allo 0,5%. Detrazione di 200 euro, più altri 50 per ogni figlio convivente. Stangata sulla...\",\n" + 
				"    \"authors\": \"Stefano Poggi Longostrevi,*(Ass. italiana dottori commercialisti").build();
		List<ScoredItem> cat = textClassifier.classify(doc);
		//System.out.println( "found "+ docs_.size()+ " docs");
		assertEquals(cat.get(0),"Economia");
		//Precision precision= new Precision(hclassifier, hclassifier);
	}
	
   
}
