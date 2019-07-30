package com.crs4.sem.rest.serializer;

import java.io.IOException;

import com.crs4.sem.model.Term;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;



public class TermSerializer extends StdSerializer<Term>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected TermSerializer(Class<?> t, boolean dummy) {
		super(t, dummy);
		// TODO Auto-generated constructor stub
	}
	
	public TermSerializer(){
		this(null);
	}

	public TermSerializer(Class<Term> t){
		super(t);
	}
	@Override
	public void serialize(Term term, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        jgen.writeStringField("content", term.content());
        if(term.hasTag()) jgen.writeStringField("tag", term.tag());
        jgen.writeEndObject();
    
	}

}
