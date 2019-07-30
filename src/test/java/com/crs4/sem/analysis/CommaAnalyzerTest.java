package com.crs4.sem.analysis;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;

public class CommaAnalyzerTest {
	
	

	@Test
	public void textSplitingCommas() throws IOException{
	Analyzer analyzer = new CommaAnalyzer();
	TokenStream stream = analyzer.tokenStream(null,
			new StringReader("l'altra 3.4. (prima) frase. seconda frase. terza frase A.C.R.O.N.I.M.O."
				+ " ieri , mentre guardavo la tv, ha suonato il campanello. forma contratta I'd 3.3. "));
	stream.reset();
	while (stream.incrementToken()) {
		System.out.println(stream.getAttribute(CharTermAttribute.class).toString());
	}
	analyzer.close();

	}
}
