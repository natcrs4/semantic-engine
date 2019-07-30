package com.crs4.sem.model;

import java.util.Date;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.NaturalId;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.impl.BuiltinIterableBridge;
import org.hibernate.tuple.GenerationTiming;

import com.crs4.sem.search.bridges.IterableJsonBridge;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.hash.Hashing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CacheConcurrencyStrategy;
@Data
@Builder
@Indexed
@NoArgsConstructor
@AllArgsConstructor
@Analyzer(impl=ItalianAnalyzer.class)
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table( name="NEWDOCUMENT" )
public class NewDocument implements Documentable{
	
	@Id
	private String internal_id;
	
    @NaturalId	
    @EqualsAndHashCode.Exclude
    @Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
	private Long id;
	
	
	
	@Lob
	@Analyzer(impl=StandardAnalyzer.class)
	 @EqualsAndHashCode.Exclude
    @Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
    private String url;
	
	@Lob
    @Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
	 @EqualsAndHashCode.Exclude
	private String title;
	
	@Lob
   @Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
	 @EqualsAndHashCode.Exclude
	private String description;
 
	@Lob
    @Analyzer(impl=StandardAnalyzer.class)
	 @EqualsAndHashCode.Exclude
    @Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
	private String authors;
    
    

    @Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
    @Analyzer(impl=WhitespaceAnalyzer.class)
    @EqualsAndHashCode.Exclude
	private String type;
    
    @Lob
	@Analyzer(impl=StandardAnalyzer.class)
    @Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
    @EqualsAndHashCode.Exclude
    private String image;
    
    
   
    @Field(index=Index.YES, analyze=Analyze.NO, store=Store.YES)
    @EqualsAndHashCode.Exclude
	private String source_id;

    
    @Field(index=Index.YES, analyze=Analyze.NO, store=Store.YES)
    @IndexedEmbedded
    @EqualsAndHashCode.Exclude
	private Date publishDate;
    


    @IndexedEmbedded
	@ManyToMany(fetch = FetchType.LAZY)
	@Cascade(value = { CascadeType.SAVE_UPDATE})
	@JoinTable( name="LINKS")
	@FieldBridge(impl=IterableJsonBridge.class)
	@Analyzer(impl=StandardAnalyzer.class)
	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
    @EqualsAndHashCode.Exclude
	 private Set<Page>  links;
	

	 @IndexedEmbedded
	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(value = { CascadeType.SAVE_UPDATE})
	@JoinTable( name="MOVIES")
	 @FieldBridge(impl=IterableJsonBridge.class)
	@Analyzer(impl=StandardAnalyzer.class)
	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
	 @EqualsAndHashCode.Exclude
	private Set<NewMetadata> movies;
    
    
	 @IndexedEmbedded
	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(value = { CascadeType.SAVE_UPDATE})
	@JoinTable( name="GALLERY")
	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
	@Analyzer(impl=StandardAnalyzer.class)
	 @FieldBridge(impl=IterableJsonBridge.class)
	 @EqualsAndHashCode.Exclude
   	private Set<NewMetadata>  gallery;
    
 
	 @IndexedEmbedded
	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(value = { CascadeType.SAVE_UPDATE})
	@JoinTable( name="ATTACHMENTS")
	@Analyzer(impl=StandardAnalyzer.class)
	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
	 @FieldBridge(impl=IterableJsonBridge.class)
	 @EqualsAndHashCode.Exclude
   	private Set<NewMetadata>  attachments;
    

	@IndexedEmbedded
	@ManyToMany(fetch = FetchType.EAGER)
	@Cascade(value = { CascadeType.SAVE_UPDATE})
	@JoinTable( name="PODCASTS")
	@Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
	@FieldBridge(impl=IterableJsonBridge.class)
	 @EqualsAndHashCode.Exclude
   	private Set<NewMetadata> podcasts;
    
    
    @Field(index=Index.YES, analyze=Analyze.NO, store=Store.YES)
    @EqualsAndHashCode.Exclude
   	private Float score;
    
    @Field(index=Index.YES, analyze=Analyze.NO, store=Store.YES)
    @EqualsAndHashCode.Exclude
 	private Long neoid;
    
    @Field(index=Index.YES, analyze=Analyze.NO, store=Store.YES)
    @EqualsAndHashCode.Exclude
    @IndexedEmbedded
   	private Date timestamp;
    
	
   
    
   
 
    @Field(index=Index.YES, analyze=Analyze.NO, store=Store.YES)
    @Column(columnDefinition="boolean default false")
    @EqualsAndHashCode.Exclude
    private Boolean trainable=false;

	//@Override
	public String text() {
		return this.getTitle()!=null?this.getTitle():""+" "+this.getDescription()!=null?this.getDescription():"";
	}
	
	 @Column(columnDefinition="text")
    @IndexedEmbedded
    @Field(index=Index.YES, analyze=Analyze.NO, store=Store.YES)
	 @EqualsAndHashCode.Exclude
   	private String [] keywords;
	
    @Column(columnDefinition="text")
	@IndexedEmbedded
	@Field(index=Index.YES, analyze=Analyze.NO, store=Store.YES)
    @EqualsAndHashCode.Exclude
	private String [] categories;
	
    @Column(columnDefinition="text")
    @IndexedEmbedded
    @Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
    @Analyzer(impl=StandardAnalyzer.class)
    @EqualsAndHashCode.Exclude
   	private String [] entities;
   
	public static Long setHashID(byte []  s){
		Long l= Hashing.md5().hashBytes(s).asLong();
		return Math.abs(l);
	}

	public Document toDocument() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void assignIdentifiers() {
		if(this.getAttachments()!=null)
		for(NewMetadata mt:this.getAttachments()) {
			mt.setId(DigestUtils.md5Hex(mt.getUrl()));
		}
		if(this.getGallery()!=null)
		for(NewMetadata mt:this.getGallery()) {
			mt.setId(DigestUtils.md5Hex(mt.getUrl()));
		}
		if(this.getMovies()!=null)
		for(NewMetadata mt:this.getMovies()) {
			mt.setId(DigestUtils.md5Hex(mt.getUrl()));
		}
		if(getPodcasts()!=null)
	   for(NewMetadata mt:this.getPodcasts()) {
				mt.setId(DigestUtils.md5Hex(mt.getUrl()));
			}
	   if(getLinks()!=null)
		   for(Page mt:this.getLinks()) {
			mt.setId(DigestUtils.md5Hex(mt.getUrl())) ;
		}
			
	}
	
}
