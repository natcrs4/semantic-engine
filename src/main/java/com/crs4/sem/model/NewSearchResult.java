package com.crs4.sem.model;

import java.util.List;

import com.crs4.sem.model.SearchResult.SearchResultBuilder;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NewSearchResult {
	
	private Integer totaldocs;
	private List<NewDocument> documents;
	private List<NewDocument> duplicated;
	private List<PairStringInteger> keywords;
	private List<PairStringInteger> categories;
	private List<PairStringInteger> dates;
	private List<PairStringInteger> authors;
	private List<PairStringInteger> types;
	
}
