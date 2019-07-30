package com.crs4.sem.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttributeImpl;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeFactory;

public class JFlexCommaTokenizer extends Tokenizer {
	/** A private instance of the JFlex-constructed scanner */
	private SentenceTokenizerImpl scanner;
	public static int ALPHANUM=0;
	public static int APOSTROPHE=1;
	public static int ACRONYM=2;
	public static int COMPANY=3;
	public static int EMAIL=4;
	public static int HOST=5;
	public static int NUM=6;
	public static int CJ=7;
	public static int ACRONYM_DEP=8;
	public static int SOUTHEAST_ASIAN=9;
	public static int IDEOGRAPHIC=10;
	public static int HIRAGANA=11;
	public static int KATAKANA=12;
	public static int HANGUL=13;
	public static int ACRONYM_NUM=14;
	public static int COMMAS=15;
	public static int P=16;
	/** String token types that correspond to token type int constants */
	public static final String[] TOKEN_TYPES = new String[] { "<ALPHANUM>", "<APOSTROPHE>", "<ACRONYM>", "<COMPANY>",
			"<EMAIL>", "<HOST>", "<NUM>", "<CJ>", "<ACRONYM_DEP>", "<SOUTHEAST_ASIAN>", "<IDEOGRAPHIC>", "<HIRAGANA>",
			"<KATAKANA>", "<HANGUL>" ,"<ACRONYM_NUM>", "<COMMAS>","<P>"};
	public static final int MAX_TOKEN_LENGTH_LIMIT = 1024 * 1024;

	private int skippedPositions;
	private boolean returnPunctuation;

	private int maxTokenLength = StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH;

	/**
	 * Set the max allowed token length. No tokens longer than this are emitted.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given length is outside of the range [1,
	 *             {@value #MAX_TOKEN_LENGTH_LIMIT}].
	 */
	public void setMaxTokenLength(int length) {
		if (length < 1) {
			throw new IllegalArgumentException("maxTokenLength must be greater than zero");
		} else if (length > MAX_TOKEN_LENGTH_LIMIT) {
			throw new IllegalArgumentException("maxTokenLength may not exceed " + MAX_TOKEN_LENGTH_LIMIT);
		}
		if (length != maxTokenLength) {
			maxTokenLength = length;
			scanner.setBufferSize(length);
		}
	}

	public JFlexCommaTokenizer(AttributeFactory factory) {
		super(factory);
		init();
	}

	public JFlexCommaTokenizer() {
		this.returnPunctuation=false;
		init();
	}

	public JFlexCommaTokenizer(boolean returnPunctuaction) {
		this.returnPunctuation=returnPunctuaction;
		init();
	}
	private void init() {
		this.scanner = new SentenceTokenizerImpl(input);
	}

	// this tokenizer generates three attributes:
	// term offset, positionIncrement and type
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
	private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.lucene.analysis.TokenStream#next()
	 */
	@Override
	public final boolean incrementToken() throws IOException {
		clearAttributes();
		CharTermAttribute buffer = new CharTermAttributeImpl();

		skippedPositions = 0;

		while (true) {
			int tokenType = scanner.getNextToken();

			if (tokenType == SentenceTokenizerImpl.YYEOF) {
				if (termAtt.length() > 0)
					return true;
				return false;
			}
			
			if (tokenType == SentenceTokenizerImpl.P&&!this.returnPunctuation)
				return true;
			if (scanner.yylength() <= maxTokenLength) {
				posIncrAtt.setPositionIncrement(skippedPositions + 1);
				scanner.getText(buffer);
				//if((COMMAS!=tokenType)&&(P!=tokenType)) 
				
				if((COMMAS==tokenType)&&this.returnPunctuation)
					termAtt.append(buffer);
				else if(COMMAS==tokenType)
					return true;
				if(COMMAS!=tokenType)
					termAtt.append(buffer);
				termAtt.append(' ');
				final int start = scanner.yychar();
				offsetAtt.setOffset(correctOffset(start), correctOffset(start + termAtt.length()));
				typeAtt.setType(JFlexCommaTokenizer.TOKEN_TYPES[tokenType]);
				// return true;
			} else
				// When we skip a too-long term, we still increment the
				// position increment
				skippedPositions++;
			if (tokenType == P&&this.returnPunctuation)
				return true;
			if (tokenType == COMMAS&&this.returnPunctuation)
				return true;
		}
	}

	@Override
	public final void end() throws IOException {
		super.end();
		// set final offset
		int finalOffset = correctOffset(scanner.yychar() + scanner.yylength());
		offsetAtt.setOffset(finalOffset, finalOffset);
		// adjust any skipped tokens
		posIncrAtt.setPositionIncrement(posIncrAtt.getPositionIncrement() + skippedPositions);
	}

	@Override
	public void close() throws IOException {
		super.close();
		scanner.yyreset(input);
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		scanner.yyreset(input);
		skippedPositions = 0;
	}
}
