package com.crs4.sem.rest.jaxrs.provider;

import java.util.Date;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.crs4.sem.model.Link;
import com.crs4.sem.model.SearchResult;
import com.crs4.sem.model.Term;
import com.crs4.sem.rest.serializer.DateSerializer;
import com.crs4.sem.rest.serializer.LinkSerializer;
import com.crs4.sem.rest.serializer.SearchResultSerializer;
import com.crs4.sem.rest.serializer.TermSerializer;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Provider
public class MyJacksonJsonProvider implements ContextResolver<ObjectMapper> {
	    
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    static {
      MAPPER.setSerializationInclusion(Include.NON_EMPTY);
      MAPPER.enable(MapperFeature.USE_GETTERS_AS_SETTERS);
   
      SimpleModule module = new SimpleModule();
      module.addSerializer(Term.class, new TermSerializer());
      module.addSerializer(Date.class, new DateSerializer());
      module.addSerializer(SearchResult.class, new SearchResultSerializer());
      MAPPER.registerModule(module);
      
      //MAPPER.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    }
 
    public MyJacksonJsonProvider() {
        System.out.println("Instantiate MyJacksonJsonProvider");
    }
     
    @Override
    public ObjectMapper getContext(Class<?> type) {
        System.out.println("MyJacksonProvider.getContext() called with type: "+type);
        return MAPPER;
    } 

	}