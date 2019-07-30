package com.crs4.sem.model.lucene;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;

import com.crs4.sem.model.Document;
import com.crs4.sem.model.NewDocument;
import com.crs4.sem.model.NewMetadata;
import com.crs4.sem.model.Page;

public class EntityDocumentBuilder {
	
	  public static enum Fields
	  {
		   id,url, title, description,authors, type,image, source_id, internal_id, publishDate, links,movies,gallery, attachments,podcasts,score,neoid,timestamp, entities, categories,trainable,keywords;
	  }
	
	public static  Document convert(org.apache.lucene.document.Document document) {
		Document doc= new Document();
		if(document.getField(Fields.id.toString())!=null)
				doc.setId(Long.valueOf(document.getField(Fields.id.toString()).stringValue()));
		if(document.getField(Fields.url.toString())!=null)
		doc.setUrl(document.getField(Fields.url.toString()).stringValue());
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
		doc.setLinks(	document.getValues(Fields.links.toString()));
		if(document.getField(Fields.movies.toString())!=null)
		doc.setMovies(	document.getValues(Fields.movies.toString()));
		if(document.getField(Fields.gallery.toString())!=null)
		doc.setGallery(	document.getValues(Fields.gallery.toString()));
		if(document.getField(Fields.attachments.toString())!=null)
		doc.setAttachments(	document.getValues(Fields.attachments.toString()));
		if(document.getField(Fields.podcasts.toString())!=null)
		doc.setPodcasts(	document.getValues(Fields.podcasts.toString()));
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
	public static  NewDocument convertToNewDocument(org.apache.lucene.document.Document document,Map<String,NewMetadata> metadatas,Map<String,Page> pages ) {
		NewDocument doc= new NewDocument();
		//if(document.getField(Fields.id.toString())!=null)
				//doc.setId(Long.valueOf(document.getField(Fields.id.toString()).stringValue()));
		if(document.getField(Fields.url.toString())!=null)
		doc.setUrl(document.getField(Fields.url.toString()).stringValue());
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
		    doc.setLinks(	toPage(document.getValues(Fields.links.toString()), pages));
		if(document.getField(Fields.movies.toString())!=null)
		doc.setMovies(	toMetadata(document.getValues(Fields.movies.toString()),metadatas));
		if(document.getField(Fields.gallery.toString())!=null)
		doc.setGallery(	toMetadata(document.getValues(Fields.gallery.toString()),metadatas));
		if(document.getField(Fields.attachments.toString())!=null)
		doc.setAttachments(	toMetadata(document.getValues(Fields.attachments.toString()),metadatas));
		if(document.getField(Fields.podcasts.toString())!=null)
		doc.setPodcasts(	toMetadata(document.getValues(Fields.podcasts.toString()),metadatas));
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
	public static Set<NewMetadata> toMetadata(String [] links, Map<String,NewMetadata> metadatas){
		Set<NewMetadata> result= new HashSet<NewMetadata>();
		for( String link:links) {
			NewMetadata elem = metadatas.get(link);
			if (elem!=null) result.add(elem);
			else {
				elem=new NewMetadata(link);
				result.add(elem);
				metadatas.put(link, elem);
			}
		}
			
		return result;
	}
	
	public static Set<Page> toPage(String [] links, Map<String,Page> metadatas){
		Set<Page> result= new HashSet<Page>();
		for( String link:links) {
			Page elem = metadatas.get(link);
			if (elem!=null) result.add(elem);
			else {
				elem=new Page(link);
				result.add(elem);
				metadatas.put(link, elem);
			}
		}
			
		return result;
	}
	
	
}
