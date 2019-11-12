package com.crs4.sem.service;

import java.io.File;

import javax.persistence.SharedCacheMode;

import org.hibernate.cfg.Configuration;

import com.crs4.sem.model.Author;
import com.crs4.sem.model.Link;
import com.crs4.sem.model.Shado;

import net.sf.ehcache.CacheManager;

public class HibernateConfigurationFactory {
	
	
	public static Configuration configureDocumentService(File cfgFile ) {
		Configuration configuration = new Configuration();
		//configuration = configuration.addAnnotatedClass(Document.class);
		//configuration = configuration.addAnnotatedClass(Keyword.class);
		//CacheManager.getInstance().shutdown();
		configuration= configuration.configure(cfgFile);
		//configuration.setSharedCacheMode(SharedCacheMode.ALL);
		return configuration;
		
	}
	
	public static Configuration configureAuthorService(File cfgFile ) {
		Configuration configuration = new Configuration();
		//configuration = configuration.addAnnotatedClass(Author.class);
		configuration= configuration.configure(cfgFile);
		return configuration;
		
	}

	public static Configuration configureShadoService(File cfgFile) {
		Configuration configuration = new Configuration();
		configuration = configuration.addAnnotatedClass(Link.class);
		configuration = configuration.addAnnotatedClass(Shado.class);
		configuration= configuration.configure(cfgFile);
		return configuration;
		
	}

}
