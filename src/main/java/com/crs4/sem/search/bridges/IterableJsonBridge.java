package com.crs4.sem.search.bridges;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.ContainerBridge;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.builtin.IterableBridge;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class IterableJsonBridge  extends IterableBridge implements ContainerBridge{

	public IterableJsonBridge(FieldBridge bridge) {
		super(bridge);
		// TODO Auto-generated constructor stub
	}
	
	public  IterableJsonBridge() {
		this(null);
		
	}
	@Override
	public void set(String fieldName, Object value, Document document, LuceneOptions luceneOptions) {
		if ( value != null ) {
		indexNotNullIterable( fieldName, value, document, luceneOptions );
			
		}
	}
	private void indexNotNullIterable(String name, Object value, Document document, LuceneOptions luceneOptions) {
		Iterable<?> collection = (Iterable<?>) value;
		for ( Object entry : collection ) {
			indexEntry( name, entry, document, luceneOptions );
		}
	}
//	private void indexNotNullIterable(String name, Object value, Document document, LuceneOptions luceneOptions) {
//		Iterable<?> collection = (Iterable<?>) value;
//		ObjectMapper objectMapper = new ObjectMapper();
//		objectMapper.setSerializationInclusion(Include.NON_NULL);
//		
//		StringWriter writer= new StringWriter();
//		try {
//			objectMapper.writeValue(writer, value);
//			luceneOptions.addFieldToDocument(name, writer.toString(), document);
//		} catch (JsonGenerationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (JsonMappingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		}
	
	private void indexEntry(String fieldName, Object entry, Document document, LuceneOptions luceneOptions) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		
		StringWriter writer= new StringWriter();

			try {
				objectMapper.writeValue(writer, entry);
				luceneOptions.addFieldToDocument(fieldName, writer.toString(), document);
			} catch (JsonGenerationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	}
	
}
