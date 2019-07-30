package com.crs4.sem.producers;

import java.io.File;
import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.hibernate.cfg.Configuration;

import com.crs4.sem.config.SemEngineConfig;
import com.crs4.sem.service.HibernateConfigurationFactory;
import com.crs4.sem.service.NewDocumentService;

import lombok.Data;
@Data
public class DocumentProducer {
	  
	 @Inject
	  private SemEngineConfig config;
	

	  
	  
	  @Produces
	  @ApplicationScoped
	  @DocumentProducerType(ServiceType.DOCUMENT)
	  public NewDocumentService producer() {
		
		    String path=config.getHibernateCFGDocuments();
		    if (path.startsWith("classpath:")) {
		    	path=path.replace("classpath:", config.classpath()+"/applications/"+config.applicationame()+"/WEB-INF/classes/");
		    }
		   File cfgFile=  new File(path);
		    Configuration configure = HibernateConfigurationFactory.configureDocumentService(cfgFile);
		   
			NewDocumentService docservice= new NewDocumentService(configure);
		    return docservice;
	  }
	  

	
	public void close(@Disposes  @DocumentProducerType(ServiceType.DOCUMENT) NewDocumentService documentService) {

		   documentService.close();

		}
		
	}


