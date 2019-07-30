package com.crs4.sem.lucene.similarity;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.search.Query;
import org.neo4j.graphalgo.impl.PageRankResult;


public class EntityRankScoreQuery extends CustomScoreQuery{
	private PageRankResult pageRankResult;
	
	
	public PageRankResult getPageRankResult() {
		return pageRankResult;
	}


	public void setPageRankResult(PageRankResult pageRankResult) {
		this.pageRankResult = pageRankResult;
	}


	public EntityRankScoreQuery(Query subQuery, FunctionQuery scoringQuery,PageRankResult pageRankResult) {
		super(subQuery, scoringQuery);
		this.setPageRankResult(pageRankResult);
	}
	@Override
	protected CustomScoreProvider getCustomScoreProvider(LeafReaderContext context) throws IOException {
	    return new EntityRankRankProvider(context);
	  }
	public EntityRankScoreQuery(Query subQuery,PageRankResult pageRankResult) {
		super(subQuery);
		this.setPageRankResult(pageRankResult);
	}

	private class EntityRankRankProvider extends CustomScoreProvider{
 
		public EntityRankRankProvider(LeafReaderContext context) {
			super(context);
			// TODO Auto-generated constructor stub
		}
	@Override
	 public float customScore(int doc, float subQueryScore, float valSrcScore) throws IOException {
		    
		    LeafReader reader = this.context.reader();
		    Document docu = reader.document(doc);
		    long neoid = docu.getField("neoid").numericValue().longValue();
		    Double rankscore = pageRankResult.score(neoid);
		    return (subQueryScore) * valSrcScore*(1+rankscore.floatValue());
		  }
	
	
	}
	

}
