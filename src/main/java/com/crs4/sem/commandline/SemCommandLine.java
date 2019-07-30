package com.crs4.sem.commandline;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.h2.tools.Recover;
import org.h2.tools.RunScript;
import org.hibernate.cfg.Configuration;

import com.crs4.sem.config.SemEngineConfig;
import com.crs4.sem.neo4j.exceptions.CategoryNotFoundInTaxonomyException;
import com.crs4.sem.neo4j.exceptions.TaxonomyNotFoundException;
import com.crs4.sem.neo4j.service.TaxonomyService;
import com.crs4.sem.producers.AnalyzerProducer;
import com.crs4.sem.producers.ClassifierProducer;
import com.crs4.sem.service.DocumentService;
import com.crs4.sem.service.HibernateConfigurationFactory;
import com.crs4.sem.service.NewDocumentService;
import com.mfl.sem.classifier.text.TextClassifier;

public class SemCommandLine {

	public static void main(String args[]) throws TaxonomyNotFoundException, CategoryNotFoundInTaxonomyException, SQLException, IOException {
		Options options = new Options();
		//Option option_migrate = Option.builder().argName("migrate").desc("used for migrate, need two hibernate cfg files").numberOfArgs(2).build();
		//Option option_reindex = Option.builder().argName("reindex").desc("re-index").numberOfArgs(1).build();
		Option option_migrate = OptionBuilder.withArgName("migrate").hasArgs(3).withValueSeparator()
				.withDescription("used for migrate, need two hibernate cfg files").create("migrate");
		Option option_reindex = OptionBuilder.withArgName("reindex").hasArgs(1).withValueSeparator()
				.withDescription("reindex").create("reindex");
		Option classify = Option.builder().longOpt("classify").argName("classify").desc("classify, needs the config.path").numberOfArgs(1).build();
		Option detect = Option.builder().longOpt("detect").argName("detect").desc("detect keywords, needs the config path").numberOfArgs(1).build();
		Option migted = Option.builder().longOpt("migdet").argName("migdet").desc("migrate ,detect keywords and classify, needs the config path and two hibernate cfg").numberOfArgs(3).build();
		Option recover = Option.builder().longOpt("recover").argName("recover").desc("recover h2 db").numberOfArgs(1).build();
		Option runscript = Option.builder().longOpt("runscript").argName("runscript").desc("recover h2 db").numberOfArgs(1).build();
		Option lucene= Option.builder().longOpt("recindex").argName("recindex").desc("recover db from index").numberOfArgs(2).build();
		options.addOption(option_migrate);
		options.addOption(option_reindex);
		options.addOption(classify);
		options.addOption(detect);
		options.addOption(migted);
		options.addOption(recover);
		options.addOption(runscript);
		options.addOption(lucene);
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("utility-name", options);

			System.exit(1);
		}
		if (cmd.hasOption("migrate")) {
			String[] values = cmd.getOptionValues("migrate");
			File cfgFileh2 = new File(values[0]);
			
			Configuration configureh2 = HibernateConfigurationFactory.configureDocumentService(cfgFileh2);
			DocumentService source = new DocumentService(configureh2);
			File cfgFile = new File(values[1]);
			Configuration configure = HibernateConfigurationFactory.configureDocumentService(cfgFile);
			DocumentService destination = new DocumentService(configure);
			source.migrate(destination, Boolean.parseBoolean(values[2]));
            System.exit(0);
		}
		
		if (cmd.hasOption("reindex")) {
			String value = cmd.getOptionValue("reindex");
			File cfgFileh2 = new File(value);
			Configuration configureh2 = HibernateConfigurationFactory.configureDocumentService(cfgFileh2);
			DocumentService docservice = new DocumentService(configureh2);
			docservice.rebuildIndex();
		}
		if (cmd.hasOption("classify")) {
			
			String value = cmd.getOptionValue("classify");
			System.setProperty("sem.engine.basedirectory", value);
			SemEngineConfig semEngineConfig = ConfigFactory.create(SemEngineConfig.class,System.getProperties());
			File cfgFileh2 = new File(semEngineConfig.getHibernateCFGDocuments());
			Configuration configureh2 = HibernateConfigurationFactory.configureDocumentService(cfgFileh2);
			NewDocumentService docservice = new NewDocumentService(configureh2);
			
			TaxonomyService taxonomyService= new TaxonomyService(new File(semEngineConfig.neo4jDirectory()));
			AnalyzerProducer analyzerProducer = new AnalyzerProducer();
			//analyzerProducer.init();
			Analyzer analyzer= analyzerProducer.produces();
			ClassifierProducer classifierProducer = ClassifierProducer.builder().taxoservice(taxonomyService).analyzer(analyzer).docservice(docservice).build();
			classifierProducer.init();
			TextClassifier textClassifier=classifierProducer.producer();
			docservice.classifyAll(textClassifier);
		}
		
if (cmd.hasOption("detect")) {
			
			String value = cmd.getOptionValue("detect");
			System.setProperty("sem.engine.basedirectory", value);
			SemEngineConfig semEngineConfig = ConfigFactory.create(SemEngineConfig.class);
			File cfgFileh2 = new File(semEngineConfig.getHibernateCFGDocuments());
			Configuration configureh2 = HibernateConfigurationFactory.configureDocumentService(cfgFileh2);
			DocumentService docservice = new DocumentService(configureh2);
			
			TaxonomyService taxonomyService= new TaxonomyService(new File(semEngineConfig.neo4jDirectory()));
			AnalyzerProducer analyzerProducer = new AnalyzerProducer();
			//analyzerProducer.init();
			Analyzer analyzer= analyzerProducer.produces();
		
			Set<String> keywords = taxonomyService.getAllKeywords("root", true);
			docservice.detectKeywords(keywords);
		}
if (cmd.hasOption("migdet")) {
	
	String values[] = cmd.getOptionValues("migdet");
	System.setProperty("sem.engine.basedirectory", values[0]);
	SemEngineConfig semEngineConfig = ConfigFactory.create(SemEngineConfig.class);
	File cfgFileh2 = new File(values[1]);
	Configuration configureh2 = HibernateConfigurationFactory.configureDocumentService(cfgFileh2);
	NewDocumentService docservice = new NewDocumentService(configureh2);
	
	TaxonomyService taxonomyService= new TaxonomyService(new File(semEngineConfig.neo4jDirectory()));
	AnalyzerProducer analyzerProducer = new AnalyzerProducer();
	//analyzerProducer.init();
	Analyzer analyzer= analyzerProducer.produces();
	ClassifierProducer classifierProducer = ClassifierProducer.builder().taxoservice(taxonomyService).analyzer(analyzer).docservice(docservice).build();
	classifierProducer.init();
	TextClassifier textClassifier=classifierProducer.producer();
	Set<String> keywords = taxonomyService.getAllKeywords("root", true);
	

	
	File cfgFile = new File(values[2]);
	Configuration configure = HibernateConfigurationFactory.configureDocumentService(cfgFile);
	NewDocumentService destination = new NewDocumentService(configure);
	docservice.migrate(destination, textClassifier,keywords);
}
if (cmd.hasOption("recover")) {
	String values[] = cmd.getOptionValues("recover");
	Recover rec = new Recover();
	String name=values[0].replaceAll(".h2.db","");
	String comm="-db "+name;
    rec.runTool(comm.split(" "));
	//String name=values[0].replaceAll(".h2.db","");
	 //comm="-url jdbc:h2:./"+name+" -user sa -script " +name+"__"+".h2.sql";
			
	//String arg[]=comm.split(" ");
///	RunScript.main(arg);
	
}
if (cmd.hasOption("runscript")) {
	String values[] = cmd.getOptionValues("runscript");
	String name=values[0];

	String comm="-url jdbc:h2:./"+"recovered"+" -continueOnError -user sa -script " +name;
			
	String arg[]=comm.split(" ");
	RunScript.main(arg);
	
}

if (cmd.hasOption("recindex")) {
	String values[] = cmd.getOptionValues("recindex");
	String source=values[0];
	File cfgFile = new File(values[1]);
	Configuration configure = HibernateConfigurationFactory.configureDocumentService(cfgFile);
	DocumentService destination = new DocumentService(configure);
	Path path = Paths.get(source);
	Directory directory = FSDirectory.open(path);
    IndexReader indexReader = DirectoryReader.open(directory);
    int num = indexReader.numDocs()	;
    System.out.println(" found index whith size "+num);
    List<Document> documents= new ArrayList<Document>();
    for(int i=0;i<num;i++) {
    	Document document;
     
		try {
			document = indexReader.document(i);
			if(document!=null) 
				documents.add(document);
			   if(i%200==0) { 
		     destination.addAllLuceneDocument(documents);
		     documents= new ArrayList<Document>();
		     System.out.println("added "+ i + " documents");
			   }
		} catch ( Exception e) {
			// TODO Auto-generated catch block
	      e.printStackTrace();
	      
		}
    }
    		indexReader.close();
    		directory.close();
    	
    }


	


System.out.println("finished");
		
	}
	
}
