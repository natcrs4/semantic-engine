package com.crs4.sem.producers;

import java.io.File;
import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.aeonbits.owner.ConfigFactory;

import com.crs4.sem.config.SemEngineConfig;
import com.crs4.sem.service.DocumentService;
import com.crs4.sem.service.NERService;

@ApplicationScoped
public class NERProducer {
	 @Inject
	  private SemEngineConfig config;

	@Produces
	public NERService producer() throws IOException {
		
		File file = new File(config.icabparameters());
		return new NERService(file);
	}
}
