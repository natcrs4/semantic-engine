package com.crs4.sem.analysis;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.WordlistLoader;
import org.junit.Test;

// TODO: Auto-generated Javadoc
/**
 * Esempio di utilizzo di un analizzatore di Lucene.
 */
public class ItalianAnalyzerTest {
	
	/**
	 * Test analyze.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Test
	public void testAnalyze() throws IOException {
		
		
		//File file = new File("src/test/resources/stopwords_en.txt");
		//CharArraySet stopwords = WordlistLoader.getWordSet(new FileReader(file));
		Analyzer analyzer = new ItalianClassifierAnalyzer();
		TokenStream stream = analyzer.tokenStream(null,
				new StringReader("ieri ci siamo comprati il gelato"));
		stream.reset();
		while (stream.incrementToken()) {
			System.out.println(stream.getAttribute(CharTermAttribute.class).toString());
			System.out.println(stream.getAttribute(TypeAttribute.class).type());
		}
		analyzer.close();
	}


}
