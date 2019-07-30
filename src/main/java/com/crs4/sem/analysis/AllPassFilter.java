package com.crs4.sem.analysis;

import org.apache.lucene.analysis.TokenStream;



public final class AllPassFilter extends PosFilter {
	

	public AllPassFilter(TokenStream input) {
		super(input);
		// TODO Auto-generated constructor stub
	}

	public boolean isAccepted(char[] termBuffer, int i, int length, String type) {
		
		
		return true;
	}

}
