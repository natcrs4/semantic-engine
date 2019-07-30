package com.crs4.sem.producers;

import java.io.File;
import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import com.crs4.sem.config.SemEngineConfig;

public class GraphDatabaseProducer implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Inject
	private SemEngineConfig config;

	@Produces
	@GraphDatabaseProducerType(GraphServiceType.NEO4J)
	@ApplicationScoped
	public GraphDatabaseService produces() {
		
		return new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File(config.neo4jDirectory()))
				.setConfig(GraphDatabaseSettings.pagecache_memory, "512M")
				.setConfig(GraphDatabaseSettings.string_block_size, "60")
				.setConfig(GraphDatabaseSettings.array_block_size, "300").newGraphDatabase();

	}
	
	public void close(@Disposes @GraphDatabaseProducerType(GraphServiceType.NEO4J) GraphDatabaseService service) {
		service.shutdown();
	}

}
