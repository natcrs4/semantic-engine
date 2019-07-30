package com.crs4.sem.rest.serializer;


import java.io.IOException;
import java.util.Date;

import com.crs4.sem.model.SearchResult;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class SearchResultSerializer extends StdSerializer<SearchResult>{

	protected SearchResultSerializer(Class<?> t, boolean dummy) {
		super(t, dummy);
		// TODO Auto-generated constructor stub
	}
	public SearchResultSerializer(){
		this(null);
	}
	
	public SearchResultSerializer(Class<SearchResult> t){
		super(t);
	}
	@Override
	public void serialize(SearchResult value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonGenerationException {
		
		jgen.writeStartObject();
		 if(value.getTotaldocs()!=null)
			 jgen.writeObjectField("totaldocs", value.getTotaldocs());
		if(value.getPdocuments()==null&&value.getDocuments()!=null)
			jgen.writeObjectField("documents", value.getDocuments());
		if(value.getPdocuments()!=null)
			jgen.writeObjectField("documents", value.getPdocuments());
		 if(value.getTypes()!=null)
			 jgen.writeObjectField("types", value.getTypes());
		
		 if(value.getAuthors()!=null)
			 jgen.writeObjectField("authors", value.getAuthors());
		 if(value.getCategories()!=null)
			 jgen.writeObjectField("categories", value.getCategories());
		 if(value.getKeywords()!=null)
			 jgen.writeObjectField("keywords", value.getKeywords());
		 if(value.getDates()!=null)
			 jgen.writeObjectField("dates", value.getDates());
		 
		
		jgen.writeEndObject();
	}

}
