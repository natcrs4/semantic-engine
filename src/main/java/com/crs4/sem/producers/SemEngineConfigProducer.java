package com.crs4.sem.producers;

import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.aeonbits.owner.ConfigFactory;

import com.crs4.sem.config.SemEngineConfig;


@ApplicationScoped
public class SemEngineConfigProducer {
	
	 @Produces
	  public SemEngineConfig producer(InjectionPoint ip){
		      
	           
			   return ConfigFactory.create(SemEngineConfig.class,System.getProperties(),   System.getenv());
			 } 

}
