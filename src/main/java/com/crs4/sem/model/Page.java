package com.crs4.sem.model;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.ClassBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import com.crs4.sem.model.NewMetadata.NewMetadataBuilder;
import com.crs4.sem.search.bridges.MetadataBridge;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
//@Cacheable
//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE,region="page")
@Table( name="PAGE" )
@Indexed
@EqualsAndHashCode
//@ClassBridge(impl=MetadataBridge.class)
public class Page{
	@JsonIgnore
	@Id
	@EqualsAndHashCode.Exclude
	private String id;

	@Lob
	//@Field(index=Index.YES, analyze=Analyze.NO, store=Store.YES)
	private String url;

	@Lob
	//@Field(index=Index.YES, analyze=Analyze.NO, store=Store.YES)
	@EqualsAndHashCode.Exclude 
	private String newurl;
	
	public Page(String url) {
	    String md5 = DigestUtils.md5Hex(url);
	    this.setUrl(url);
	    this.setId(md5);
	}
}