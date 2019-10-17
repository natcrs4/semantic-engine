package com.crs4.sem.producers;

import java.io.File;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.hibernate.cfg.Configuration;

import com.crs4.sem.config.SemEngineConfig;
import com.crs4.sem.service.AuthorService;
import com.crs4.sem.service.HibernateConfigurationFactory;

import lombok.Data;

@Data

public class AuthorProducer {
	  @Inject
	  private SemEngineConfig config;
	
	 
	  @Produces
	  @ApplicationScoped
	  @DocumentProducerType(ServiceType.AUTHORS)
	  public AuthorService producer(){
		  
		  String path=config.getHibernateCFGAuthors();
		    if (path.startsWith("classpath:")) {
		    	path=path.replace("classpath:", config.classpath()+"/applications/"+config.applicationame()+"/WEB-INF/classes/");
		    }
		   File cfgFile=  new File(path);
		   
		    Configuration configuration=HibernateConfigurationFactory .configureAuthorService(cfgFile);
			AuthorService authorService= AuthorService.newInstance(configuration);		
		    return authorService;
	  }
	  
	  

	  
	public void close(@Disposes    @DocumentProducerType(ServiceType.AUTHORS) AuthorService authorService) {

		   authorService.close();

		}

}
