package com.crs4.sem.rest.serializer;

import java.io.IOException;
import java.text.SimpleDateFormat;

import com.crs4.sem.model.Document;
import com.crs4.sem.model.Term;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class DocumentSerializer 
	extends StdSerializer<Document>{

		/**
		 * 
		 */
	private static SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd" );
		private static final long serialVersionUID = 1L;

		protected DocumentSerializer(Class<?> t, boolean dummy) {
			super(t, dummy);
			// TODO Auto-generated constructor stub
		}
		
		public DocumentSerializer(){
			this(null);
		}

		public DocumentSerializer(Class<Document> t){
			super(t);
		}
		@Override
		public void serialize(Document doc, JsonGenerator jgen, SerializerProvider provider) throws IOException {
			
	        jgen.writeStartObject();
	        
	        jgen.writeStringField("date", df.format(doc.getPublishDate()));
	       // if(term.hasTag()) jgen.writeStringField("tag", term.tag());
	        jgen.writeEndObject();
	    
		}

	


}
