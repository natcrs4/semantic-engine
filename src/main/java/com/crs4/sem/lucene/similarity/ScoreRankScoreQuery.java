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

import lombok.Data;
import lombok.NoArgsConstructor;



public class ScoreRankScoreQuery extends CustomScoreQuery{
	

	

	public ScoreRankScoreQuery(Query subQuery, FunctionQuery scoringQuery) {
		super(subQuery, scoringQuery);
		
	}
	public ScoreRankScoreQuery(Query subQuery) {
		super(subQuery);
		
	}
	@Override
	protected CustomScoreProvider getCustomScoreProvider(LeafReaderContext context) throws IOException {
	    return new ScoreRankRankProvider(context);
	  }
	

	private class ScoreRankRankProvider extends CustomScoreProvider{
 
		public ScoreRankRankProvider(LeafReaderContext context) {
			super(context);
			// TODO Auto-generated constructor stub
		}
	@Override
	 public float customScore(int doc, float subQueryScore, float valSrcScore) throws IOException {
		    
		    LeafReader reader = this.context.reader();
		    Document docu = reader.document(doc);
		    float rankscore=0;
		    if(docu.getField("score")!=null)
		    rankscore= docu.getField("score").numericValue().floatValue();
		    return (subQueryScore) * valSrcScore*(1+rankscore);
		  }
	
	
	}
	

}
