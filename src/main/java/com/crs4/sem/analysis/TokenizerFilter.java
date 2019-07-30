package com.crs4.sem.analysis;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;           
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;


import lombok.Getter;
import lombok.Setter;

public final class TokenizerFilter extends TokenFilter {

	@Setter
	@Getter
	private Tokenizer tokenizer;
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
	@Setter
	@Getter
	private Queue<String> queue;

	public TokenizerFilter(Tokenizer tokenizer, TokenStream tokenStream) {
		super(tokenStream);

		this.setTokenizer(tokenizer);
		this.setQueue(new LinkedList<String>());
	}

	@Override
	public boolean incrementToken() throws IOException {
		if (this.getQueue().isEmpty() && this.input.incrementToken()) {
			String aux = new String(this.termAtt.buffer(), 0, this.termAtt.length());
			Reader reader = new StringReader(aux);
			this.getTokenizer().setReader(reader);
			this.getTokenizer().reset();
			CharTermAttribute attr = this.getTokenizer().addAttribute(CharTermAttribute.class);
			while (this.getTokenizer().incrementToken())
				this.getQueue().add(attr.toString());
			this.getTokenizer().close();
		}
		if (!this.getQueue().isEmpty()) {
			String token = this.getQueue().poll();
			termAtt.copyBuffer(token.toCharArray(), 0, token.length());
			//typeAtt.setType(token.getTag().getAbbr());
			return true;
		}
		return false;

	}

}
