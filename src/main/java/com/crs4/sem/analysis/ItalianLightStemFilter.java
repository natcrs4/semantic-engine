package com.crs4.sem.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.it.ItalianLightStemmer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

public final class ItalianLightStemFilter extends TokenFilter {
	  private final ItalianLightStemmer stemmer = new ItalianLightStemmer();
	  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	  private final KeywordAttribute keywordAttr = addAttribute(KeywordAttribute.class);
	  private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
	  public ItalianLightStemFilter(TokenStream input) {
	    super(input);
	  }
	  
	  @Override
	  public boolean incrementToken() throws IOException {
	    if (input.incrementToken()) {
	      if (!keywordAttr.isKeyword()) {
	        final int newlen = stemmer.stem(termAtt.buffer(), termAtt.length());
	        termAtt.setLength(newlen);
	      }
	      return true;
	    } else {
	      return false;
	    }
	  }
	}
