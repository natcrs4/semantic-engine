package com.crs4.sem.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharacterUtils;

public class TrimFilter extends TokenFilter {
	  private final CharacterUtils charUtils = CharacterUtils.getInstance();
	  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	  
	  /**
	   * Create a new LowerCaseFilter, that normalizes token text to lower case.
	   * 
	   * @param in TokenStream to filter
	   */
	  public TrimFilter(TokenStream in) {
	    super(in);
	  }
	  
	  @Override
	  public final boolean incrementToken() throws IOException {
	    if (input.incrementToken()) {
	    	 char[] copia = termAtt.buffer();
	    	 String aux=new String(copia,0,termAtt.length());
	    	 aux=aux.trim();
	     termAtt.copyBuffer(aux.toCharArray(), 0, aux.length()); 
	      return true;
	    } else
	      return false;
	  }
	}

