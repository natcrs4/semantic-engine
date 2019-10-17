package com.crs4.sem.jaxrs.application;



import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;

import org.aeonbits.owner.ConfigFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jersey2.InstrumentedResourceMethodApplicationListener;
import com.crs4.sem.config.SemEngineConfig;
import com.crs4.sem.rest.AuthorRestResources;
import com.crs4.sem.rest.ClassifierRestResources;
import com.crs4.sem.rest.NERRestReources;
import com.crs4.sem.rest.SemanticEngineRestResources;
import com.crs4.sem.rest.ShadoRestResources;
import com.crs4.sem.rest.TaxonomyRestResuorces;


public class Application extends ResourceConfig {
	 public static final MetricRegistry METRIC_REGISTRY = new MetricRegistry();
    public Application(String myPackage, String host, String port) {
        super();
        SemEngineConfig config = ConfigFactory.create(SemEngineConfig.class,System.getProperties(),System.getenv());
        String myPackages = String.format("%s, io.swagger.resources", myPackage);
        packages(myPackage);

        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.0");
        beanConfig.setHost(String.format("%s:%s", host, port));
        beanConfig.setBasePath("/"+config.applicationame()+"/rest");
        beanConfig.setResourcePackage(myPackages);
        beanConfig.setScan(true);

        registerClasses(ApiListingResource.class);
        registerClasses(SwaggerSerializers.class);
        //registerClasses(com.crs4.sem.rest.jaxrs.provider.GsonProvider.class);
        registerClasses(com.crs4.sem.rest.filters.CORSFilter.class);
        //features
        //this will register Jackson JSON providers
        registerClasses(org.glassfish.jersey.jackson.JacksonFeature.class);
        //we could also use this:
        //resources.add(com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider.class);
        
        //instead let's do it manually:
        register(MultiPartFeature.class);
        registerClasses(com.crs4.sem.rest.jaxrs.provider.MyJacksonJsonProvider.class);
        registerClasses(com.crs4.sem.rest.DocumentRestResources.class);
        registerClasses(AuthorRestResources.class);
        registerClasses(TaxonomyRestResuorces.class);
        registerClasses(ShadoRestResources.class);
        registerClasses(NERRestReources.class);
        registerClasses(ClassifierRestResources.class);
        registerClasses(SemanticEngineRestResources.class);
        
        register(new InstrumentedResourceMethodApplicationListener(METRIC_REGISTRY));
    }
}