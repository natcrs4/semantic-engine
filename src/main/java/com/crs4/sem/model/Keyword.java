package com.crs4.sem.model;

import java.util.Date;

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
import com.crs4.sem.model.Document.DocumentBuilder;

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
public class Keyword {
	
	@Id
	@GeneratedValue	
	private Long id;
	
	@Analyzer(impl=StandardAnalyzer.class)
    @Field(index=Index.YES, analyze=Analyze.YES, store=Store.YES)
	public String forma;

}
