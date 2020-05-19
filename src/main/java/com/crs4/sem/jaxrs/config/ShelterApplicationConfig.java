package com.crs4.sem.jaxrs.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.aeonbits.owner.ConfigFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import com.crs4.sem.config.SemEngineConfig;
import com.crs4.sem.rest.AuthorRestResources;
import com.crs4.sem.rest.ClassifierRestResources;
import com.crs4.sem.rest.NERRestReources;
import com.crs4.sem.rest.ShadoRestResources;
import com.crs4.sem.rest.TaxonomyRestResuorces;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;

@ApplicationPath("/rest")
public class ShelterApplicationConfig{// extends Application {
	public ShelterApplicationConfig() {
      
		
		SemEngineConfig config = ConfigFactory.create(SemEngineConfig.class,System.getProperties(),System.getenv());
		String myPackage="com.crs4.sem.rest";
        String host=config.host();
        String port=config.port();
        String myPackages = String.format("%s, io.swagger.resources", myPackage);
       

        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.0");
        beanConfig.setHost(String.format("%s:%s", host, port));
        beanConfig.setBasePath(config.applicationame()+"/rest/");
        beanConfig.setResourcePackage(myPackages);
        beanConfig.setScan(true);
	}
 
	
	
    //@Override
    public Set<Class<?>> getClasses() {
        
        Set<Class<?>> resources = new HashSet<Class<?>>();
        
        System.out.println("REST configuration starting: getClasses()");            
        
      
        resources.add(com.crs4.sem.rest.filters.CORSFilter.class);
        //features
        //this will register Jackson JSON providers
        resources.add(org.glassfish.jersey.jackson.JacksonFeature.class);
        //we could also use this:
        //resources.add(com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider.class);
        
        //instead let's do it manually:
        resources.add(com.crs4.sem.rest.jaxrs.provider.MyJacksonJsonProvider.class);
        resources.add(com.crs4.sem.rest.DocumentRestResources.class);
        //resources.add(TaxonomyRestResuorces.class);
        //resources.add(AuthorRestResources.class);
        //resources.add(NERRestReources.class);
        resources.add(ApiListingResource.class);
        resources.add(SwaggerSerializers.class);
        //resources.add(ClassifierRestResources.class);
        resources.add(MultiPartFeature.class);
        resources.add(ShadoRestResources.class);
        //resources.add(it.crs4.stt.recommender.filters.CORSFilter.class);
        
        //==> we could also choose packages, see below getProperties()
        
        System.out.println("REST configuration ended successfully.");
        
        return resources;
    }
    
   // @Override
    public Set<Object> getSingletons() {
        return Collections.emptySet();
    }
    

    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<String, Object>();
        
        //in Jersey WADL generation is enabled by default, but we don't 
        //want to expose too much information about our apis.
        //therefore we want to disable wadl (http://localhost:8080/service/application.wadl should return http 404)
        //see https://jersey.java.net/nonav/documentation/latest/user-guide.html#d0e9020 for details
        properties.put("jersey.config.server.wadl.disableWadl", true);
        
        //we could also use something like this instead of adding each of our resources
        //explicitly in getClasses():
        //properties.put("jersey.config.server.provider.packages", "com.nabisoft.tutorials.mavenstruts.service");
        
        
        return properties;
    }    
}
