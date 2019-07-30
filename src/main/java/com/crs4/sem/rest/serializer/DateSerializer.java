package com.crs4.sem.rest.serializer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.crs4.sem.model.Term;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class DateSerializer extends StdSerializer<Date>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
	protected DateSerializer(Class<?> t, boolean dummy) {
		super(t, dummy);
		// TODO Auto-generated constructor stub
	}
	
	public DateSerializer(){
		this(null);
	}

	public DateSerializer(Class<Date> t){
		super(t);
	}
	@Override
	public void serialize(Date date, JsonGenerator jgen, SerializerProvider provider) throws IOException {
       
        jgen.writeString( df.format(date));
        
    
	}

}