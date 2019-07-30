package com.crs4.sem.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

public abstract class PosFilter extends TokenFilter {

	protected final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
	public PosFilter(TokenStream input) {
		super(input);
	}

	@Override
	public boolean incrementToken() throws IOException {
		if (input.incrementToken()) {
			char termBuffer[] = termAtt.buffer();
		
			int length = termAtt.length();
			int newLength;
			if (this.isAccepted(termBuffer, 0, length,typeAtt.type()))
				newLength = length;// this.indexWithOutTag(termBuffer, 0,
									// length);
			else
				newLength = 0;
			termAtt.setLength(newLength);
			return true;
		} else
			return false;
	}

	public abstract boolean isAccepted(char[] termBuffer, int i, int length, String type);

	private int indexWithOutTag(char[] termBuffer, int i, int length) {
		for (int j = i; i < length; j++)
			if (termBuffer[j] == '/')
				return j;
		return length;
	}
}
