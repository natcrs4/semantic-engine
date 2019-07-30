package com.crs4.sem.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.util.CharacterUtils;

import lombok.Data;

@Data
public class DoubleFilter extends TokenFilter {
	


	private final CharacterUtils charUtils = CharacterUtils.getInstance();
	  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
		private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
		
	  protected DoubleFilter(TokenStream input) {
		super(input);
		// TODO Auto-generated constructor stub
	}
	  /**
	   * Create a new LowerCaseFilter, that normalizes token text to lower case.
	   * 
	   * @param in TokenStream to filter
	   */
	  
	  
	
	  
	  @Override
	  public final boolean incrementToken() throws IOException {
	    if (input.incrementToken()) {
	      char[] copia = termAtt.buffer();
	     
	      typeAtt.setType(new String(copia,0,termAtt.length()));
	      return true;
	    } else
	      return false;
	  }
	}

