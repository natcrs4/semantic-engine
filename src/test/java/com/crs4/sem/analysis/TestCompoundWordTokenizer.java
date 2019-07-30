package com.crs4.sem.analysis;

import java.io.FileReader;
import java.io.StringReader;

import org.apache.lucene.analysis.compound.CompoundWordTokenFilterBase;
import org.apache.lucene.analysis.compound.HyphenationCompoundWordTokenFilter;
import org.apache.lucene.analysis.compound.hyphenation.HyphenationTree;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import com.sun.tools.hat.internal.parser.Reader;

public class TestCompoundWordTokenizer {

//	public void testHyphenationCompoundWordsDE() throws Exception {
//	     String[] dict = { "Rind", "Fleisch", "Draht", "Schere", "Gesetz",
//	         "Aufgabe", "Überwachung" };
//	 
//	     FileReader reader = new FileReader("de_DR.xml");
//	 
//	     HyphenationTree hyphenator = HyphenationCompoundWordTokenFilter
//	         .getHyphenationTree(reader.toString());
//	 
//	     HyphenationCompoundWordTokenFilter tf = new HyphenationCompoundWordTokenFilter(
//	         new WhitespaceTokenizer(new StringReader(
//	             "Rindfleischüberwachungsgesetz Drahtschere abba")), hyphenator,
//	         dict, CompoundWordTokenFilterBase.DEFAULT_MIN_WORD_SIZE,
//	         CompoundWordTokenFilterBase.DEFAULT_MIN_SUBWORD_SIZE,
//	         CompoundWordTokenFilterBase.DEFAULT_MAX_SUBWORD_SIZE, false);
//	         
//	     CharTermAttribute t = tf.addAttribute(CharTermAttribute.class);
//	     while (tf.incrementToken()) {
//	        System.out.println(t);
//	     }
//	   }
}
