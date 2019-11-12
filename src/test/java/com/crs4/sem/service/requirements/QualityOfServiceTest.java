package com.crs4.sem.service.requirements;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.crs4.sem.model.NewSearchResult;

public class QualityOfServiceTest {
	
	
	@Test
	public void testPerformanceLocale() throws IOException {
	
		String host="http://localhost:8080/semantic-engine/";
		perftest(host);
	}

	@Test
	public void testPerformanceSosEngine() throws IOException {
	
		String host="http://sosengine.cultur-e.it:8080/semantic-engine-sos/";
		perftest(host);
	}
	private void perftest(String host) throws IOException {
		List<String> lines = FileUtils.readLines(new File("src/test/resources/queries.txt"));
		Client client = ClientBuilder.newClient();
		WebTarget webTarget 
		  = client.target(host);
		long total=0L;
		for( String line:lines) {
			String q=line.replace("\"", "");
			q=q.replace(",", "");
			System.out.println(q);
			WebTarget searchWebTarget 
			  = webTarget.path("rest/documents/advancedsearch").queryParam("text", q);
			Invocation.Builder invocationBuilder 
			  = searchWebTarget.request(MediaType.APPLICATION_JSON);
			 long startTime = System.currentTimeMillis();
			Response resp = invocationBuilder.get();
			System.out.println(resp.getStatus());
			 long stopTime = System.currentTimeMillis();
			 total+=stopTime-startTime;
		}
		System.out.println(total);
		double performance=lines.size()*1000;
		performance=performance/(total);
		System.out.println(performance);
	}

}
