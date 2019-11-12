package com.crs4.sem.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Entity
@Indexed
@NoArgsConstructor
@AllArgsConstructor
//@DynamicUpdate
@Analyzer(impl=ItalianAnalyzer.class)
public class Document implements Documentable{
	
	@Id
	@GeneratedValue	
	private Long id;
	
	
	
	
	@Column(columnDefinition="text")
	@Analyzer(impl=StandardAnalyzer.class)
    @Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
    public String url;
	
    @Column(columnDefinition="text")
    @Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
	public String title;
	
   //@Lob
   //@Column( length = 100000 )
   @Column(columnDefinition="text")
   @Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
	public String description;
 
    @Column(columnDefinition="text")
    @Analyzer(impl=StandardAnalyzer.class)
    @Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
	public String authors;
    
//    @Analyzer(impl=StandardAnalyzer.class)
//    @Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
//	public String authors;
    
    @Column(columnDefinition="text")
    @Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
    @Analyzer(impl=WhitespaceAnalyzer.class)
	public String type;
    
    @Column(columnDefinition="text")
	@Analyzer(impl=StandardAnalyzer.class)
    @Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
    public String image;
    
    
   // @Column(columnDefinition="text")
    @Field(index=Index.YES, analyze=Analyze.NO, store=Store.YES)
	public String source_id;
    
    @Field(index=Index.YES, analyze=Analyze.NO, store=Store.YES)
	public String internal_id;
    
    @Field(index=Index.YES, analyze=Analyze.NO, store=Store.YES)
    @IndexedEmbedded
	public Date publishDate;
    
    @Column(columnDefinition="text")
    @Field(index=Index.YES, analyze=Analyze.NO, store=Store.YES)
    @IndexedEmbedded
    @Analyzer(impl=StandardAnalyzer.class)
	public String [] links;
	
    @Column(columnDefinition="text")
    @Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
    @IndexedEmbedded
    @Analyzer(impl=StandardAnalyzer.class)
	public String [] movies;
    
    @Column(columnDefinition="text")
    @Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
    @IndexedEmbedded
    @Analyzer(impl=StandardAnalyzer.class)
   	public String [] gallery;
    
    @Column(columnDefinition="text")
    @Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
    @IndexedEmbedded
    @Analyzer(impl=StandardAnalyzer.class)
   	public String [] attachments;
    
    @Column(columnDefinition="text")
    @Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
    @IndexedEmbedded
    @Analyzer(impl=StandardAnalyzer.class)
   	public String [] podcasts;
    
    
    @Field(index=Index.YES, analyze=Analyze.NO, store=Store.YES)
   	public Float score;
    
    @Field(index=Index.YES, analyze=Analyze.NO, store=Store.YES)
 	public Long neoid;
    
    @Field(index=Index.YES, analyze=Analyze.NO, store=Store.YES)
    @IndexedEmbedded
   	public Date timestamp;
    
    @Column(columnDefinition="text")
    @IndexedEmbedded
    @Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
    @Analyzer(impl=StandardAnalyzer.class)
   	public String [] entities;
   
    
    @Column(columnDefinition="text")
    @IndexedEmbedded
    @Field(index=Index.YES, analyze=Analyze.NO, store=Store.YES)
    //@Analyzer(impl=StandardAnalyzer.class)
   	public String [] categories;
    
    
    //@Field(index=Index.YES, analyze=Analyze.NO, store=Store.YES)
    @Column(columnDefinition="boolean default false")
    @Field(index=Index.YES, analyze=Analyze.NO, store=Store.YES)
    public Boolean trainable=false;


    @Override
    public String text() {
		String aux="";
		aux= (this.getTitle()!=null?this.getTitle():"")+" "+(this.getDescription()!=null?this.getDescription():"");
		return aux;
	}
//    
    @Column(columnDefinition="text")
    @IndexedEmbedded
    @Field(index=Index.YES, analyze=Analyze.NO, store=Store.YES)
   	public String [] keywords;


	public void copyFields(Document doc)  {
	this.setAttachments(doc.getAttachments());
	this.setAuthors(doc.getAuthors());
	this.setCategories(doc.getCategories());
	this.setDescription(doc.getDescription());
	this.setEntities(doc.getEntities());
	this.setGallery(doc.getGallery());
	this.setTitle(doc.getTitle());
	this.setInternal_id(doc.getInternal_id());
	this.setLinks(doc.getLinks());
	this.setMovies(doc.getMovies());
	this.setNeoid(doc.getNeoid());
	this.setPodcasts(doc.getPodcasts());
	this.setPublishDate(doc.getPublishDate());
	this.setSource_id(doc.getSource_id());
	this.setScore(doc.getScore());
	this.setTrainable(doc.getTrainable());
	this.setType(doc.getType());
	this.setUrl(doc.getUrl());
	this.setKeywords(doc.getKeywords());
	this.setTimestamp(doc.getTimestamp());
	this.setSource_id(doc.getSource_id());
	this.setImage(doc.getImage());
//	Class<Document> yourClass = Document.class;
//	for (Method method : yourClass.getMethods()){
//	    //String getmethod;
//		if(method.getName().startsWith("set")) {
//	    	String getmethodname=method.getName().replace("set", "get");
//	       Method getmethod = yourClass.getMethod(getmethodname);
//	       Object value = getmethod.invoke(doc);
//		   method.invoke(this, value);    
//		}
//	}
	}


	public static Document toDocument(NewDocument doc) {
		 Document d= new Document();
		 return d;
	}


	

    
   
    
    
   // public Document(){}
    
//    public  static DocumentBuilder builder(){
//    	return new DocumentBuilder();
//    }
//    
}
