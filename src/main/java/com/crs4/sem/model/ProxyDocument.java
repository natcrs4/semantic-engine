package com.crs4.sem.model;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProxyDocument {
	private Long id;
    private String url;
	private String title;
	private String description; 
	private String authors;
	private String type;   
    private String image;
	private String source_id;  
	private String internal_id;    
	private Date publishDate;
	private List<Metadata> links;	
	private List<Metadata> movies;
   	private List<Metadata> gallery;
   	private List<Metadata> attachments; 
   	private List<Metadata> podcasts;
   	private Float score;
   	private Date timestamp;
   	private String [] entities;
   	private String [] categories;
   	private String [] keywords;

   	public void copyFields(Document doc)  {
   		//this.setAttachments(doc.getAttachments());
   		this.setId(doc.getId());
   		this.setAuthors(doc.getAuthors());
   		this.setCategories(doc.getCategories());
   		this.setDescription(doc.getDescription());
   		this.setEntities(doc.getEntities());
   		//this.setGallery(doc.getGallery());
   		this.setTitle(doc.getTitle());
   		this.setInternal_id(doc.getInternal_id());
   		//this.setLinks(doc.getLinks());
   		//this.setMovies(doc.getMovies());
   		//this.setNeoid(doc.getNeoid());
   		//this.setPodcasts(doc.getPodcasts());
   		this.setPublishDate(doc.getPublishDate());
   		this.setSource_id(doc.getSource_id());
   		this.setScore(doc.getScore());
   		
   		this.setType(doc.getType());
   		this.setUrl(doc.getUrl());
   		this.setKeywords(doc.getKeywords());
   		this.setTimestamp(doc.getTimestamp());
   		this.setImage(doc.getImage());

   		}
   	
   	
   	
}
