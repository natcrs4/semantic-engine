package com.crs4.sem.producers;

import java.io.File;
import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.io.fs.FileUtils;

import com.crs4.sem.config.SemEngineConfig;
import com.crs4.sem.neo4j.service.TaxonomyCSVReader;
import com.crs4.sem.neo4j.service.TaxonomyService;

import lombok.Builder;
import lombok.Data;

@ApplicationScoped

public class TaxonomyProducer {

	@Inject
	SemEngineConfig config;

	@Inject
	@GraphDatabaseProducerType(GraphServiceType.NEO4J)
	private GraphDatabaseService service;

	@Produces
	public TaxonomyService producer(InjectionPoint ip) throws IOException {
		TaxonomyService taxoservice = new TaxonomyService(service);
		//TaxonomyCSVReader.read(new File(config.taxonomyCsv()), taxoservice);
		return taxoservice;
	}

}
