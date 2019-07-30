package com.crs4.sem.rest.serializer;

import java.io.IOException;
import java.util.Date;

import com.crs4.sem.model.Link;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class LinkSerializer extends StdSerializer<Link>{

	protected LinkSerializer(Class<?> t, boolean dummy) {
		super(t, dummy);
		// TODO Auto-generated constructor stub
	}
	
	public LinkSerializer(){
		this(null);
	}

	public LinkSerializer(Class<Link> t){
		super(t);
	}

	@Override
	public void serialize(Link value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonGenerationException {
		 jgen.writeStartObject();
		    jgen.writeString(value.getLink());
		    jgen.writeString(value.getNewLink());
		    jgen.writeEndObject();
		
	}

}
