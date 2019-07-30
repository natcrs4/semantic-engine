package com.crs4.sem.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Tokenizer;

public class SentenceAnalyzer extends Analyzer{
	@Override
	protected TokenStreamComponents createComponents(String field) {
		Tokenizer tokenizer = new JFlexSentenceTokenizer(true);
		TokenFilter filter = new AllPassFilter(tokenizer);
		return new TokenStreamComponents(tokenizer, filter);
	}


}
