package com.crs4.sem.service.requirements;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.hibernate.cfg.Configuration;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.io.fs.FileUtils;

import com.crs4.sem.analysis.ItalianClassifierAnalyzer;
import com.crs4.sem.model.Document;
import com.crs4.sem.model.Documentable;
import com.crs4.sem.neo4j.exceptions.CategoryNotFoundInTaxonomyException;
import com.crs4.sem.neo4j.service.TaxonomyCSVReader;
import com.crs4.sem.neo4j.service.TaxonomyService;
import com.crs4.sem.service.HibernateConfigurationFactory;
import com.crs4.sem.service.NewDocumentService;
import com.crs4.sem.utils.DocumentsUtil;
import com.mfl.sem.classifier.HClassifier;
import com.mfl.sem.classifier.HClassifierBuilder;
import com.mfl.sem.classifier.exception.ClassifierException;
import com.mfl.sem.classifier.impl.EnsembleClassifier;
import com.mfl.sem.classifier.impl.SVMClassifier;
import com.mfl.sem.classifier.model.Category;
import com.mfl.sem.classifier.model.CategoryDictionary;
import com.mfl.sem.classifier.model.Dictionary;
import com.mfl.sem.classifier.performance.ClassifierEvaluator;
import com.mfl.sem.classifier.performance.Evaluator;
import com.mfl.sem.classifier.performance.MacroPrecision;
import com.mfl.sem.classifier.performance.MacroRecall;
import com.mfl.sem.classifier.performance.MicroPrecision;
import com.mfl.sem.classifier.performance.MicroRecall;
import com.mfl.sem.classifier.performance.PMeasure;
import com.mfl.sem.classifier.performance.Precision;
import com.mfl.sem.classifier.performance.TextPrecision;
import com.mfl.sem.classifier.policy.MajoritySelection;
import com.mfl.sem.classifier.text.DocItem;
import com.mfl.sem.classifier.text.Documents;
import com.mfl.sem.classifier.text.TextClassifier;
import com.mfl.sem.classifier.text.impl.EnsembleTextClassifier;
import com.mfl.sem.classifier.text.impl.TextClassifierImpl;
import com.mfl.sem.dataset.reader.DocumentReader;
import com.mfl.sem.dataset.reader.News20Reader;
import com.mfl.sem.model.Dataset;
import com.mfl.sem.text.model.Doc;

public class RequirementTest {
	
	  public Analyzer produces(){
			Analyzer analyzer;
			Map<String, Analyzer> mapanalyzer=new HashMap<String,Analyzer>();
	  	// { "id", "url", "title", "description", "authors", "type", "source_id", "internal_id",
				//	"publishDate", "links", "movies", "gallery", "attachments", "podcasts", "score", "neoid", "entities",
				//	"categories", "trainable" };
	  	mapanalyzer.put("url", new StopAnalyzer());
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
	  @Test
	  public void testSOSHierarchical() throws InstantiationException, IllegalAccessException, IOException, ClassifierException, CategoryNotFoundInTaxonomyException {
		  Analyzer analyzer = this.produces();
		  File neodirectory = new File("/tmp/test");
		   if(neodirectory.exists())
			   FileUtils.deleteRecursively(neodirectory);;
		   TaxonomyService taxoservice = new TaxonomyService(new File("/tmp/test"));
		   TaxonomyCSVReader.readTriple(new FileInputStream(new File("src/test/resources/SOS_270119_Tassonomia_rev5.csv")), taxoservice);
			CategoryDictionary categoryDictionary = new CategoryDictionary();
			Node root = taxoservice.searchCategory("root");
			HClassifier<SVMClassifier> hclassifier;
			String path="configurations/locale/hibernate.lucene.cfg2.xml";
			File cfgFile=  new File(path);
		    Configuration configure = HibernateConfigurationFactory.configureDocumentService(cfgFile);
		   
			NewDocumentService docservice= NewDocumentService.newInstance(configure);
				hclassifier = HClassifierBuilder.builder().species(SVMClassifier.class).root(root)
						.taxonomyService(taxoservice).categoryBuilder(categoryDictionary).build();
			   SVMClassifier svm = new SVMClassifier();
				hclassifier.setLevel1(false);
			TextClassifierImpl textClassifier = new TextClassifierImpl(analyzer,svm,categoryDictionary);
			List<Documentable> docs= new ArrayList<Documentable>();
			List<Documentable> docs_ = docservice.getTrainable();
			String[] categories = taxoservice.branchLabels(root, false);
			List<Documentable> kdocs = new ArrayList<Documentable>();
			
			
			for (String category : categories) {
				String[] keywords;
				keywords = taxoservice.getKetwords("root", category);
				//if(category.trim().length()==0) System.out.println("_____");
				String[] categories_ = new String[1];
				categories_[0] = category;
				if(keywords!=null) 
				for (int i = 0; i < keywords.length; i++) {
					Documentable doc = Document.builder().title(keywords[i]).categories(categories_).build();
	                kdocs.add(doc);
				}
			}
			//docs.addAll(kdocs);
			if(!docs_.isEmpty())docs.addAll(docs_);
			docs=DocumentsUtil.expandUniLabel(docs);
			Collections.shuffle(docs);
			
			DocumentsUtil.parentize(docs, taxoservice);
			
			Documents documents = new DocumentReader(docs);
			//textClassifier.train(kdocsreader);
		  Documents testset=new DocumentReader(docs_);
			
				
				
				 
				  TextPrecision precision =  TextPrecision.builder().documents(documents).textClassifier(textClassifier).testset(testset).build();
				  precision.setTrainset(documents);
				  precision.split(0.8);
				  double result=precision.calculate();
				  assertEquals(result,0.891,0.01);
			  }
	  

	
	  
	  @Test
	  public void testTaxoSVM() throws IOException, ClassifierException, CategoryNotFoundInTaxonomyException {
		  Analyzer analyzer = this.produces();
		  File neodirectory = new File("/tmp/test");
		   if(neodirectory.exists())
			   FileUtils.deleteRecursively(neodirectory);;
		   TaxonomyService taxoservice = new TaxonomyService(new File("/tmp/test"));
		   TaxonomyCSVReader.readTriple(new FileInputStream(new File("src/test/resources/SOS_270119_Tassonomia_rev5.csv")), taxoservice);
			CategoryDictionary categoryDictionary = new CategoryDictionary();
			Node root = taxoservice.searchCategory("root");
			HClassifier<SVMClassifier> hclassifier;
			String path="configurations/locale/hibernate.lucene.cfg2.xml";
			File cfgFile=  new File(path);
		    Configuration configure = HibernateConfigurationFactory.configureDocumentService(cfgFile);
		   
			NewDocumentService docservice= NewDocumentService.newInstance(configure);
				//hclassifier = HClassifierBuilder.builder().species(SVMClassifier.class).root(root)
						//.taxonomyService(taxoservice).categoryBuilder(categoryDictionary).build();
			   // SVMClassifier svm = new SVMClassifier();
				//hclassifier.setLevel1(false);
		
			List<Documentable> docs= new ArrayList<Documentable>();
			List<Documentable> docs_ = docservice.getTrainable();
			String[] categories = taxoservice.branchLabels(root, false);
			List<Documentable> kdocs = new ArrayList<Documentable>();
			
			
			for (String category : categories) {
				String[] keywords;
				keywords = taxoservice.getKetwords("root", category);
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
			Collections.shuffle(docs);
			
			DocumentsUtil.parentize(docs, taxoservice);
			Documents documents = new DocumentReader(docs);
			//textClassifier.train(kdocsreader);
		  Documents testset=new DocumentReader(docs_);
		  SVMClassifier svm [] = new SVMClassifier[10];
		  for(int i=0;i<10;i++)
			  svm[i]= new SVMClassifier();
		  EnsembleClassifier ensemble = EnsembleClassifier.builder().size(10).group(svm).selectionPolicy(new MajoritySelection()).build();
		  
		  EnsembleTextClassifier textClassifier= new EnsembleTextClassifier(10);
		  textClassifier.setAnalyzer(analyzer);
		  textClassifier.setClassifier(ensemble);
		  textClassifier.setCategoryDictionary(new CategoryDictionary());
		  textClassifier.setDictionary(new Dictionary());
		 
		
		 
		  TextPrecision precision = TextPrecision.builder().documents(documents).textClassifier(textClassifier).build();
		  precision.split(0.6d);
		  double result=precision.calculate();
		  assertEquals(result,0.891,0.01);
	  }
	 
	  
	  @Test
		public void testEvaluation() throws FileNotFoundException, IOException, ClassifierException, InstantiationException, IllegalAccessException, CategoryNotFoundInTaxonomyException{
			long tot=6000L;
			  Analyzer analyzer = this.produces();
			  File neodirectory = new File("/tmp/test");
			   if(neodirectory.exists())
				   FileUtils.deleteRecursively(neodirectory);;
			   TaxonomyService taxoservice = new TaxonomyService(new File("/tmp/test"));
			   TaxonomyCSVReader.readTriple(new FileInputStream(new File("src/test/resources/SOS_270119_Tassonomia_rev5.csv")), taxoservice);
				CategoryDictionary categoryDictionary = new CategoryDictionary();
				Node root = taxoservice.searchCategory("root");
				HClassifier<SVMClassifier> hclassifier;
				String path="configurations/locale/hibernate.lucene.cfg2.xml";
				File cfgFile=  new File(path);
			    Configuration configure = HibernateConfigurationFactory.configureDocumentService(cfgFile);
			   
				NewDocumentService docservice= NewDocumentService.newInstance(configure);
					hclassifier = HClassifierBuilder.builder().species(SVMClassifier.class).root(root)
							.taxonomyService(taxoservice).categoryBuilder(categoryDictionary).build();
				   SVMClassifier svm = new SVMClassifier();
					hclassifier.setLevel1(false);
				TextClassifierImpl textClassifier = new TextClassifierImpl(analyzer,hclassifier,categoryDictionary);
				List<Documentable> docs= new ArrayList<Documentable>();
				List<Documentable> docs_ = docservice.getTrainable();
				String[] categories = taxoservice.branchLabels(root, false);
				List<Documentable> kdocs = new ArrayList<Documentable>();
				
				
				for (String category : categories) {
					String[] keywords;
					keywords = taxoservice.getKetwords("root", category);
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
				docs=DocumentsUtil.expandUniLabel(docs);
				Collections.shuffle(docs);
				
		DocumentsUtil.parentize(docs, taxoservice);
				
				DocumentReader documents = new DocumentReader(docs);
				//textClassifier.train(kdocsreader);
				Documents[] splitted = documents.split(0.6);
			  Documents testset=splitted[1];
			  Documents trainset=splitted[0];
			textClassifier.train(trainset);
			 
			  //TextPrecision precision = TextPrecision.builder().documents(documents).textClassifier(textClassifier).build();
			  
			Set<Category> set= new HashSet<Category>(textClassifier.getCategoryDictionary().getCategories().values());
			PMeasure macroPrecision = new MacroPrecision(1, set);
			Evaluator evaluator= new ClassifierEvaluator( macroPrecision,tot);
			evaluator.addDataset(testset, textClassifier);
			System.out.println("Macro Precision"+evaluator.evaluate());
			for(Category x:set)
				if(evaluator.total(x)>0) 
				System.out.println(x+ " measure "+ evaluator.evaluate(x)+ " totals "+ evaluator.total(x));
			
			MacroRecall macroRecall = new MacroRecall(macroPrecision);
			evaluator= new ClassifierEvaluator( macroRecall,tot);
			System.out.println("Macro Recall"+ evaluator.evaluate());
			for(Category x:set)
				if(evaluator.total(x)>0) 
					System.out.println(x+ " measure "+ evaluator.evaluate(x)+ " totals "+ evaluator.total(x));
			
			MicroPrecision microPrecision = new MicroPrecision(macroPrecision);
			evaluator= new ClassifierEvaluator( microPrecision,tot);
			System.out.println("Micro Precision"+ evaluator.evaluate());
			for(Category x:set)
				if(evaluator.total(x)>0) 
				System.out.println(x+ " measure "+ evaluator.evaluate(x)+ " totals "+ evaluator.total(x));
			
			MicroRecall microRecall = new MicroRecall(macroPrecision);
			evaluator= new ClassifierEvaluator( microRecall,tot);
			System.out.println("Micro Recall"+ evaluator.evaluate());
			for(Category x:set)
				if(evaluator.total(x)>0) 
				System.out.println(x+ " measure "+ evaluator.evaluate(x)+ " totals "+ evaluator.total(x));
			
		}
}
