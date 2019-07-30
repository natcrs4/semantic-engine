package com.crs4.sem.model;

import java.util.List;

import com.mfl.sem.model.ScoredItem;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class SearchResult {
	private Integer totaldocs;
	private List<Document> documents;
	private List<ProxyDocument> pdocuments;
	private List<PairStringInteger> keywords;
	private List<PairStringInteger> categories;
	private List<PairStringInteger> dates;
	private List<PairStringInteger> authors;
	private List<PairStringInteger> types;

}
