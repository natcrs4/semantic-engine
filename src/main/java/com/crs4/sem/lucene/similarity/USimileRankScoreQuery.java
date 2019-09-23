package com.crs4.sem.lucene.similarity;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.search.Query;

import info.debatty.java.stringsimilarity.JaroWinkler;
import lombok.Data;



/**
 * @author mariolocci
 *
 */
public class USimileRankScoreQuery extends CustomScoreQuery {
	
	private String url;

	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}


	public USimileRankScoreQuery(Query subQuery, String url) {
		super(subQuery);
		this.setUrl(url);
	}

	
	public USimileRankScoreQuery(Query subQuery, FunctionQuery scoringQuery) {
		super(subQuery, scoringQuery);
		
	}
	
	@Override
	protected CustomScoreProvider getCustomScoreProvider(LeafReaderContext context) throws IOException {
	    return new USimileRankRankProvider(context);
	  }
	

	private class USimileRankRankProvider extends CustomScoreProvider{
 
		public USimileRankRankProvider(LeafReaderContext context) {
			super(context);
			// TODO Auto-generated constructor stub
		}
	@Override
	 public float customScore(int doc, float subQueryScore, float valSrcScore) throws IOException {
		    
		    LeafReader reader = this.context.reader();
		    Document docu = reader.document(doc);
		    JaroWinkler d =  new JaroWinkler();
		    Double rankscore=0d;
		    
		    rankscore= d.similarity(docu.getField("url").toString(),url);
		    return (subQueryScore) * valSrcScore+rankscore.floatValue();
		  }
	
	
	}
}
