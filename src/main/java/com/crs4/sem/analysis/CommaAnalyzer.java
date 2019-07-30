package com.crs4.sem.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Tokenizer;

public class CommaAnalyzer extends Analyzer{
	@Override
	protected TokenStreamComponents createComponents(String field) {
		Tokenizer tokenizer = new JFlexCommaTokenizer(false);
		TokenFilter filter = new AllPassFilter(tokenizer);
		filter=new TrimFilter(filter);
		return new TokenStreamComponents(tokenizer, filter);
	}


}
