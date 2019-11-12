package com.crs4.sem.producers;

import java.io.File;
import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.hibernate.cfg.Configuration;

import com.crs4.sem.config.SemEngineConfig;
import com.crs4.sem.service.DocumentService;
import com.crs4.sem.service.HibernateConfigurationFactory;
import com.crs4.sem.service.ShadoService;
import com.sun.syndication.io.FeedException;

import lombok.Data;


@Data
public class ShadoServiceProducer {
	 @Inject
	  private SemEngineConfig config;
	
	 
	  @Produces
	  @ApplicationScoped
	  @DocumentProducerType(ServiceType.SHADO)
	  public ShadoService producer() {
		
		  String path=config.getHibernateCFGShado();
		    if (path.startsWith("classpath:")) {
		    	path=path.replace("classpath:", config.classpath()+"/applications/"+config.applicationame()+"/WEB-INF/classes/");
		    }
		   File cfgFile=  new File(path);
		    Configuration configure = HibernateConfigurationFactory.configureShadoService(cfgFile);
			ShadoService shadoService=  ShadoService.newInstance(configure);

		    
		    return shadoService;
	  }
	  
	  public void close(@Disposes 	 @DocumentProducerType(ServiceType.SHADO) ShadoService shadoService) {

		  shadoService.close();

		}

}
