package com.crs4.sem.search.bridges;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.lucene.document.Document;
import org.hibernate.search.annotations.Factory;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

import com.crs4.sem.model.NewMetadata;
import com.crs4.sem.model.Page;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;



public class MetadataBridge implements FieldBridge {
	public static final MetadataBridge INSTANCE = new MetadataBridge();

	@Override
	public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
		if (!((value instanceof NewMetadata ) ||(value instanceof Page )))
			throw new IllegalArgumentException("Class not supported");
		//NewMetadata aspect = (NewMetadata) value;
		if(value instanceof NewMetadata) {
			NewMetadata metadata = (NewMetadata) value;
//			//metadata.getDescription()
//			if(metadata.getDescription()!=null)
//				luceneOptions.addFieldToDocument("mdescription", metadata.getDescription(), document);
//			if(metadata.getUrl()!=null)
//			luceneOptions.addFieldToDocument("murl", metadata.getUrl(), document);
//			if(metadata.getNewurl()!=null)
//			luceneOptions.addFieldToDocument("mnewurl", metadata.getNewurl(), document);
//			if(metadata.getFormat()!=null)
//			luceneOptions.addFieldToDocument("mformat", metadata.getFormat(), document);
//			if(metadata.getDuration()!=null)
//			luceneOptions.addNumericFieldToDocument("duration", metadata.getDuration(), document);
//			if(metadata.getPoster()!=null)
//				luceneOptions.addFieldToDocument("mposter", metadata.getPoster(), document);
//			if(metadata.getTitle()!=null)
//				luceneOptions.addFieldToDocument("mtitle", metadata.getTitle(), document);
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.setSerializationInclusion(Include.NON_NULL);
			StringWriter writer= new StringWriter();
			  try {
				objectMapper.writeValue(writer, metadata);
				luceneOptions.addFieldToDocument("mjson", writer.toString(), document);
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
		if(value instanceof Page) {
		Page page=(Page) value;
//		if(page.getUrl()!=null)
//			luceneOptions.addFieldToDocument("purl", page.getUrl(), document);
//		if(page.getNewurl()!=null)
//			luceneOptions.addFieldToDocument("pnewurl", page.getNewurl(), document);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_NULL);
		StringWriter writer= new StringWriter();
		  try {
			objectMapper.writeValue(writer, page);
			luceneOptions.addFieldToDocument("pjson", writer.toString(), document);
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
		

//		if (aspect.getScore() != null)
//			//luceneOptions.addNumericDocValuesFieldToDocument(, aspect.getScore(), document);
//		{
//			DoubleField field = new DoubleField(name+"." + "s"+aspect.getName(), aspect.getScore(), Store.YES);
//			document.add(field);
//			}
//		if (aspect.getOpinion() != null){
//			//luceneOptions.addNumericDocValuesFieldToDocument(name+"." + "o"+aspect.getName(), aspect.getOpinion(),
//				//	document);
//			DoubleField field = new DoubleField(name+"." + "o"+aspect.getName(),aspect.getScore(), Store.YES);
//			document.add(field);
		//}
	}

	@Factory
	public MetadataBridge getInstance() {
		return INSTANCE;
	}
}