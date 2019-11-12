package com.crs4.sem.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import com.crs4.sem.analysis.JFlexSentenceTokenizer;
import com.crs4.sem.model.CompoundTaggedTerm;
import com.crs4.sem.model.SimpleTerm;
import com.crs4.sem.model.Tag;
import com.crs4.sem.model.TaggedTerm;
import com.crs4.sem.model.Term;
import com.google.common.base.Joiner;

import eu.danieldk.nlp.jitar.data.Model;
import eu.danieldk.nlp.jitar.languagemodel.LanguageModel;
import eu.danieldk.nlp.jitar.languagemodel.LinearInterpolationLM;
import eu.danieldk.nlp.jitar.tagger.HMMTagger;
import eu.danieldk.nlp.jitar.wordhandler.LexiconWordHandler;
import eu.danieldk.nlp.jitar.wordhandler.SuffixWordHandler;
import eu.danieldk.nlp.jitar.wordhandler.WordHandler;
import lombok.Data;

@Data
public class NERService {

	private Model model;
	private SuffixWordHandler swh;
	private WordHandler wh;
	private LanguageModel lm;
	private HMMTagger tagger;

	public NERService(File modelfile) throws IOException {
		model = Model.readModel(modelfile);
		swh = new SuffixWordHandler(model, 2, 2, 8, 4, 10, 10);
		wh = new LexiconWordHandler(model.lexicon(), model.uniGrams(), swh);

		// Create an n-gram language model.
		lm = new LinearInterpolationLM(model.uniGrams(), model.biGrams(), model.triGrams());

		// Initialize a tagger with a beam of 1000.0.
		tagger = new HMMTagger(model, wh, lm, 1000.0);
	}

	public List<Term> tag(String text) throws IOException{
		 
	    List<Term> taggedsent= new ArrayList<Term>();
	        try (BufferedReader reader = new BufferedReader(new StringReader(text))) {
	            String line;
	            while ((line = reader.readLine()) != null) {
	                String tokens[] = line.split("\\s+");

	                List<String> tags =
	                        HMMTagger.highestProbabilitySequence(tagger.tag(Arrays.asList(tokens)),
	                                model).sequence();
	            
	                for(int i=0;i<tokens.length;i++)
	                	   taggedsent.add(new TaggedTerm(tokens[i], tags.get(i)));
	               
	            }
	            return taggedsent;
	       
	    
	}
	

	}
	public  List<Term> tagSentences(String text) throws IOException {
		Tokenizer tokenizer = new JFlexSentenceTokenizer(true);
		tokenizer.reset();
		tokenizer.setReader(new StringReader(text));
		tokenizer.reset();
		//List<TaggedTerm> taggedsentence=new ArrayList<TaggedTerm>();
		CharTermAttribute attr = tokenizer.addAttribute(CharTermAttribute.class);
		TypeAttribute typeAttr = tokenizer.addAttribute(TypeAttribute.class);
		List<Term> taggedTerms=  new ArrayList<Term>();
		while (tokenizer.incrementToken()) {
			String sentence = attr.toString().trim();
			String type = typeAttr.type();
			taggedTerms.addAll(this.tag(sentence));
		}
		tokenizer.close();
		return taggedTerms;
	}	
	
	public List<Term> listOfEntities(List<Term> taggedterms){
		List<Term> tagged=new ArrayList<Term>();
		List<Term> terms=new ArrayList<Term>();
		CompoundTaggedTerm compound=null;
		for(Term x:taggedterms){
			if(x.tag().startsWith("B-")) {
			
				terms= new ArrayList<Term>();
				
				compound= new CompoundTaggedTerm(terms,new Tag(x.tag().replaceAll("B-", "")));
				tagged.add(compound);
				SimpleTerm simple = new SimpleTerm(x.content());
				terms.add(simple);
				
			}
			else
			if(x.tag().startsWith("I-")) {
				SimpleTerm simple = new SimpleTerm(x.content());
				if(terms.size()==0){
					compound= new CompoundTaggedTerm(terms,new Tag(x.tag().replaceAll("B-", "")));
					tagged.add(compound);
				}
				terms.add(simple);
				
			}
			else
				 terms=new ArrayList<Term>();
			
		}
		return tagged;
	}
    
	public List<Term> listOfPerson(List<Term> taggedterms){
		List<Term> tagged=new ArrayList<Term>();
		List<Term> terms=new ArrayList<Term>();
		CompoundTaggedTerm compound=null;
		for(Term x:taggedterms){
			if(x.tag().startsWith("B-PER")) {
			
				terms= new ArrayList<Term>();
				
				compound= new CompoundTaggedTerm(terms,new Tag(x.tag().replaceAll("B-", "")));
				tagged.add(compound);
				SimpleTerm simple = new SimpleTerm(x.content());
				terms.add(simple);
				
			}
			else
			if(x.tag().startsWith("I-PER")) {
				SimpleTerm simple = new SimpleTerm(x.content());
				if(terms.size()==0){
					compound= new CompoundTaggedTerm(terms,new Tag(x.tag().replaceAll("B-", "")));
					tagged.add(compound);
				}
				terms.add(simple);
				
			}
			else
				 terms=new ArrayList<Term>();
			
		}
		return tagged;
	}
	public List<Term> list(String text) throws IOException {
		List<Term> list = this.tagSentences(text);
		List<Term> ent = this.listOfEntities(list);
		return ent;
	}
	
	public List<Term> listOfPerson(String text) throws IOException {
		List<Term> list = this.tagSentences(text);
		List<Term> ent = this.listOfPerson(list);
		return ent;
	}
}
