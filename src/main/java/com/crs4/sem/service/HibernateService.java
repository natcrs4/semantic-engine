package com.crs4.sem.service;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import lombok.Data;

@Data
public class HibernateService {
	
	
	public   SessionFactory factory;
	public Integer BATCH_SIZE = 1000;
	
	
	public HibernateService() {
		
	}
	
	public HibernateService(Configuration configuration) {
//		ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
//				.applySettings(configuration.getProperties()).build();

		factory = configuration.buildSessionFactory();
	};


	public void close() {
	
		factory.close();
	
	}

}
