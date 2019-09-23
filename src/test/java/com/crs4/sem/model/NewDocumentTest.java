package com.crs4.sem.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.mfl.sem.text.model.Doc;

public class NewDocumentTest {
	
	@Test
	public void testSimile() {
		NewDocument doc1 = NewDocument.builder().title("ciao").description("ciao mondo").url("http://casadellalegalita.info/comunicati-e-commenti-2008-in-evidenza-82/6834-qualche-intercettazione-dalla-tangentopoli-fiorentina.html-10018").build();
		NewDocument doc2 = NewDocument.builder().title("ciao").description("ciao mondo").url("https://casadellalegalita.info/comunicati-e-commenti-2008-in-evidenza-82/6834-qualche-intercettazione-dalla-tangentopoli-fiorentina.html-4949").build();
     assertEquals(2.9, doc1.simile(doc2),0.01);
	}

}
