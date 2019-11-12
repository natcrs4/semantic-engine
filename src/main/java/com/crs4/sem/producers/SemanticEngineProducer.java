package com.crs4.sem.producers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.cfg.Configuration;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import com.crs4.sem.config.SemEngineConfig;
import com.crs4.sem.model.NewDocument;
import com.crs4.sem.neo4j.service.NodeService;
import com.crs4.sem.service.HibernateConfigurationFactory;
import com.crs4.sem.service.NERService;
import com.crs4.sem.service.NewDocumentService;
import com.crs4.sem.service.SemanticEngineService;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import lombok.Data;


@Data
public class SemanticEngineProducer {
	
	 @Inject
	  private SemEngineConfig config;
	 @Inject
	  private NERService nerservice;
	 
	 

	
	 @Produces
	 @ApplicationScoped
	  @DocumentProducerType(ServiceType.SEMANTICS)
	 public SemanticEngineService produces() throws IOException {
		
		 String path=config.getHibernateSemDocuments();
		    if (path.startsWith("classpath:")) {
		    	path=path.replace("classpath:", config.classpath()+"/applications/"+config.applicationame()+"/WEB-INF/classes/");
		    }

		 Configuration configure = HibernateConfigurationFactory.configureDocumentService(new File(path));
			NewDocumentService ndocservice= new NewDocumentService(configure);
		
		
		GraphDatabaseService graph1 = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File(config.semneo4j()))
		.setConfig(GraphDatabaseSettings.pagecache_memory, "512M")
		.setConfig(GraphDatabaseSettings.string_block_size, "60")
		.setConfig(GraphDatabaseSettings.array_block_size, "300")
		.setConfig(GraphDatabaseSettings.read_only,"false").newGraphDatabase();
		
		SemanticEngineService docservice= new SemanticEngineService(new NodeService(graph1), nerservice, ndocservice);
		
	    return docservice;
	 }

	 public void close(@Disposes  @DocumentProducerType(ServiceType.SEMANTICS) SemanticEngineService service) {
			service.shutdown();
		}
}
