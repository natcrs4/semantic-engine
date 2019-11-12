package com.crs4.sem.producers;

import java.util.logging.Logger;

import javax.enterprise.inject.Produces;
//import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

public class LoggerProducer {

	 @Produces
	  public Logger producer(InjectionPoint ip){
		 
			   return Logger.getLogger(ip.getMember().getDeclaringClass().getName()); // or sth similar, depends on your implementation  
			 } 
	}