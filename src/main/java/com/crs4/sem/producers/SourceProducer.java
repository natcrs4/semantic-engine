package com.crs4.sem.producers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.lucene.analysis.Analyzer;

import com.crs4.sem.config.SemEngineConfig;

import lombok.Data;

@Data
@RequestScoped
public class SourceProducer {
	@Inject
	SemEngineConfig config;
	
	@Produces
	//map source_id to categories
	public Map<String,String> produces() {
		String sources=config.getCategorizedSources();
		String ids=config.getSourceIdentifiers();
		
		Map<String,String> idtocat= new HashMap<String,String>();
		Map<String, String> urlcat;
		try {
			urlcat = readcsvsources(sources);
		
			Map<String, String> urlid = mapurltoid(ids);
		Set<String> urls = urlcat.keySet();
		for(String url:urls) {
			String cat=urlcat.get(url);
			String id=urlid.get(url);
			if(id!=null)
			   idtocat.put(id, cat);
			else System.out.println( "url not found "+url);
		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return idtocat;
	}

	private Map<String, String> readcsvsources(String namefile) throws FileNotFoundException, IOException {
		InputStream  inputStream=new FileInputStream(new File(namefile));
		InputStreamReader in = new InputStreamReader(inputStream);
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(';').parse(in);
		Map<String,String> urlcat=new HashMap<String,String>();
		for (CSVRecord record : records) {
			String id=record.get(0);
			String source_name=record.get(1);
			String url=record.get(2);
			String category=record.get(3);
			urlcat.put(url, category);
		
		}
		return urlcat;
	}
	
	private Map<String, String> mapurltoid(String namefile) throws FileNotFoundException, IOException {
		InputStream  inputStream=new FileInputStream(new File(namefile));
		InputStreamReader in = new InputStreamReader(inputStream);
		Iterable<CSVRecord> records = CSVFormat.DEFAULT.withDelimiter(';').parse(in);
		Map<String,String> urlid=new HashMap<String,String>();
		for (CSVRecord record : records) {
			String id=record.get(0);
			String source_name=record.get(1);
			String url=record.get(2);
		   urlid.put(url, id);
		}
		return urlid;
	}
      
      
}
