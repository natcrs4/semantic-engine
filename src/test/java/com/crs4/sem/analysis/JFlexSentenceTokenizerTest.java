package com.crs4.sem.analysis;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.junit.Test;

public class JFlexSentenceTokenizerTest {

	@Test
	public void testTokenize() throws IOException {

		Tokenizer tokenizer = new JFlexSentenceTokenizer();
		tokenizer.reset();
		tokenizer.setReader(new StringReader("l'altra 3.4. (prima) frase. seconda frase. terza frase A.C.R.O.N.I.M.O."
				+ " ieri, mentre guardavo la tv, ha suonato il campanello. forma contratta I'd 3.3. "));
		tokenizer.reset();
		CharTermAttribute attr = tokenizer.addAttribute(CharTermAttribute.class);
		TypeAttribute typeAttr = tokenizer.addAttribute(TypeAttribute.class);
		int i = 0;
		while (tokenizer.incrementToken()) {
			String term = attr.toString();
			String type = typeAttr.type();
			System.out.println(term + " " + type);
			i++;
		}
		tokenizer.close();
		assertEquals("Test if have 6 sentences", 6, i);
	}
	
	@Test
	public void testTokenize2() throws IOException {

		Tokenizer tokenizer = new JFlexSentenceTokenizer();
		tokenizer.reset();
		tokenizer.setReader(new StringReader("l'altra 3.4. (prima) frase. seconda frase. terza frase A.C.R.O.N.I.M.O."
				+ " ieri, mentre guardavo la tv, ha suonato il campanello. forma contratta I'd 3.3. "));
		tokenizer.reset();
		CharTermAttribute attr = tokenizer.addAttribute(CharTermAttribute.class);
		TypeAttribute typeAttr = tokenizer.addAttribute(TypeAttribute.class);
		int i = 0;
		while (tokenizer.incrementToken()) {
			String term = attr.toString();
			String type = typeAttr.type();
			System.out.println(term + " " + type);
			i++;
		}
		tokenizer.close();
		assertEquals("Test if have 6 sentences", 6, i);
	}
	@Test
	public void testTokenizeReturnPunctuaction() throws IOException {

		Tokenizer tokenizer = new JFlexSentenceTokenizer(true);
		tokenizer.reset();
		tokenizer.setReader(new StringReader("l'altra 3.4. (prima) frase. seconda frase. terza frase A.C.R.O.N.I.M.O."
				+ " ieri, mentre guardavo la tv, ha suonato il campanello... forma contratta I'd 3.3! frase con la virgola, ecc."));
		tokenizer.reset();
		CharTermAttribute attr = tokenizer.addAttribute(CharTermAttribute.class);
		TypeAttribute typeAttr = tokenizer.addAttribute(TypeAttribute.class);
		int i = 0;
		String result="";
		while (tokenizer.incrementToken()) {
			String term = attr.toString();
			String type = typeAttr.type();
			System.out.println(term + " " + type);
			i++;
			result+=term+ " ";
		}
		tokenizer.close();
		assertTrue(result.contains("!"));
	}


}
