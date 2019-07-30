package com.crs4.sem.convertes;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.document.Document;

import com.crs4.sem.model.NewDocument;
import com.crs4.sem.model.NewMetadata;
import com.crs4.sem.model.Page;
import com.crs4.sem.model.lucene.EntityDocumentBuilder.Fields;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LuceneDocumentConverter {
	
	public static  NewDocument convertToNewDocument(Document document ) {
		NewDocument doc= new NewDocument();
		//if(document.getField(Fields.id.toString())!=null)
				//doc.setId(Long.valueOf(document.getField(Fields.id.toString()).stringValue()));
		if(document.getField(Fields.url.toString())!=null)
		doc.setUrl(document.getField(Fields.url.toString()).stringValue());
		if(document.getField(Fields.id.toString())!=null)
		doc.setId(document.getField(Fields.id.toString()).numericValue().longValue());
		if(document.getField(Fields.title.toString())!=null)
		doc.setTitle(document.getField(Fields.title.toString()).stringValue());
		if(document.getField(Fields.description.toString())!=null)
		doc.setDescription(document.getField(Fields.description.toString()).stringValue());
		if(document.getField(Fields.authors.toString())!=null)
		doc.setAuthors(document.getField(Fields.authors.toString()).stringValue());
		if(document.getField(Fields.type.toString())!=null)
		doc.setType(document.getField(Fields.type.toString()).stringValue());
		if(document.getField(Fields.image.toString())!=null)
		doc.setImage(document.getField(Fields.image.toString()).stringValue());
		if(document.getField(Fields.source_id.toString())!=null)
		doc.setSource_id(document.getField(Fields.source_id.toString()).stringValue());
		if(document.getField(Fields.internal_id.toString())!=null)
		doc.setInternal_id(document.getField(Fields.internal_id.toString()).stringValue());
		if(document.getField(Fields.publishDate.toString())!=null)
		doc.setPublishDate(new Date(document.getField(Fields.publishDate.toString()).numericValue().longValue()));
		if(document.getField(Fields.links.toString())!=null)
		    doc.setLinks(	toPage(document.getValues(Fields.links.toString())));
		if(document.getField(Fields.movies.toString())!=null)
		doc.setMovies(	toMetadata(document.getValues(Fields.movies.toString())));
		if(document.getField(Fields.gallery.toString())!=null)
		doc.setGallery(	toMetadata(document.getValues(Fields.gallery.toString())));
		if(document.getField(Fields.attachments.toString())!=null)
		doc.setAttachments(	toMetadata(document.getValues(Fields.attachments.toString())));
		if(document.getField(Fields.podcasts.toString())!=null)
		doc.setPodcasts(	toMetadata(document.getValues(Fields.podcasts.toString())));
		if(document.getField(Fields.score.toString())!=null)
		doc.setScore(Float.valueOf(document.getField(Fields.score.toString()).stringValue()));
		if(document.getField(Fields.neoid.toString())!=null)
		doc.setNeoid(Long.valueOf(document.getField(Fields.neoid.toString()).stringValue()));
		if(document.getField(Fields.timestamp.toString())!=null)
		doc.setTimestamp(new Date(Long.valueOf(document.getField(Fields.timestamp.toString()).stringValue())));
		if(document.getField(Fields.entities.toString())!=null)
		doc.setEntities(	document.getValues(Fields.entities.toString()));
		if(document.getField(Fields.categories.toString())!=null)
		doc.setCategories(	document.getValues(Fields.categories.toString()));
		if(document.getField(Fields.keywords.toString())!=null)
		doc.setKeywords(	document.getValues(Fields.keywords.toString()));
		if(document.getField(Fields.trainable.toString())!=null)
		doc.setTrainable(Boolean.valueOf(document.getField(Fields.trainable.toString()).stringValue()));
		return doc;
	}

	public static Set<NewMetadata> toMetadata(String [] links){
		Set<NewMetadata> result= new HashSet<NewMetadata>();
		   
	     ObjectMapper objectMapper2 = new ObjectMapper();
	     objectMapper2.setSerializationInclusion(Include.NON_NULL);
	
		for( String link:links) {
			NewMetadata revive;
			try {
				revive = objectMapper2.readValue(new StringReader(link), new TypeReference<NewMetadata>()
				 { });
				result.add(revive);
			} catch (JsonParseException e) {
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
		
			
		return result;
	}
	
	public static Set<Page> toPage(String [] links){
		Set<Page> result= new HashSet<Page>();
		ObjectMapper objectMapper2 = new ObjectMapper();
	     objectMapper2.setSerializationInclusion(Include.NON_NULL);
	
		for( String link:links) {
			Page revive;
			try {
				revive = objectMapper2.readValue(new StringReader(link), new TypeReference<Page>()
				 { });
				result.add(revive);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			}
		
			
		return result;
	}
			
	


}
