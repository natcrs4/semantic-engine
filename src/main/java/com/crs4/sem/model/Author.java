package com.crs4.sem.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import com.crs4.sem.analysis.CommaAnalyzer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@Entity
@Indexed
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Author {
	@Id
	private String id;
	
	//@Analyzer(impl=CommaAnalyzer.class)
    @Field(index=Index.YES, analyze=Analyze.NO, store=Store.YES)
    @EqualsAndHashCode.Exclude 
	private String authors_;
    
    @Field(index=Index.YES, analyze=Analyze.NO, store=Store.YES)
    @EqualsAndHashCode.Exclude 
	private Integer frequency;
    
    
    @Analyzer(impl=StandardAnalyzer.class)
    @Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
	private String authors;



}
