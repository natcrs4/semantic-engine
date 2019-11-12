package com.crs4.sem.service;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.benchmark.quality.Judge;
import org.apache.lucene.benchmark.quality.QualityBenchmark;
import org.apache.lucene.benchmark.quality.QualityQuery;
import org.apache.lucene.benchmark.quality.QualityQueryParser;
import org.apache.lucene.benchmark.quality.QualityStats;
import org.apache.lucene.benchmark.quality.trec.TrecJudge;
import org.apache.lucene.benchmark.quality.trec.TrecTopicsReader;
import org.apache.lucene.benchmark.quality.utils.SimpleQQParser;
import org.apache.lucene.benchmark.quality.utils.SubmissionReport;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
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
	
	public void precisionRecall() throws Exception {
		 
		 File topicsFile = new File("C:/Users/Raden/Documents/lucene/LuceneHibernate/LIA/lia2e/src/lia/benchmark/topics.txt");
		    File qrelsFile = new File("C:/Users/Raden/Documents/lucene/LuceneHibernate/LIA/lia2e/src/lia/benchmark/qrels.txt");
		    Directory dir = FSDirectory.open(  Paths.get("C:/Users/Raden/Documents/myindex"));
		  
		    IndexReader indexReader = DirectoryReader.open(dir);
		      IndexSearcher searcher = new IndexSearcher(indexReader);
		    String docNameField = "filename";
		 
		    PrintWriter logger = new PrintWriter(System.out, true);
		 
		    TrecTopicsReader qReader = new TrecTopicsReader();   //#1
		    QualityQuery qqs[] = qReader.readQueries(            //#1
		        new BufferedReader(new FileReader(topicsFile))); //#1
		 
		    Judge judge = new TrecJudge(new BufferedReader(      //#2
		        new FileReader(qrelsFile)));                     //#2
		 
		    judge.validateData(qqs, logger);                     //#3
		 
		    QualityQueryParser qqParser = new SimpleQQParser("title", "contents");  //#4
		 
		    QualityBenchmark qrun = new QualityBenchmark(qqs, qqParser, searcher, docNameField);
		    SubmissionReport submitLog = null;
		    QualityStats stats[] = qrun.execute(judge,           //#5
		            submitLog, logger);
		 
		    QualityStats avg = QualityStats.average(stats);      //#6
		    avg.log("SUMMARY",2,logger, "  ");
		    dir.close();
		  
		}

}
