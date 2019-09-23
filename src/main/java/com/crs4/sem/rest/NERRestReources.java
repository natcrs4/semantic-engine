package com.crs4.sem.rest;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import com.crs4.sem.analysis.JFlexSentenceTokenizer;
import com.crs4.sem.model.TaggedTerm;
import com.crs4.sem.model.Term;
import com.crs4.sem.model.Document;
import com.crs4.sem.model.Documentable;
import com.crs4.sem.service.NERService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

@Stateless
//@ApplicationScoped
@Path("/entities")
@Data
@Api(value = "Entity", description = "Resources api for detect semantic entities from document and text")
public class NERRestReources {
	
	@Inject
	private NERService nerservice;

	@Inject
	private Logger log;
	
	@GET
	@Path("/detect")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "detect entities from text", notes = "Detect entities from given text")
	public List<Term> identify(@QueryParam("text") String text) throws IOException {

		log.info("classify text" + text);
		
	
	return nerservice.tagSentences(text);
	}
	@POST
	@Path("/detect")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "detect entities from document", notes = "Detect entities to document")
	public List<Term> identify_post(Document doc) throws IOException {
		String text = doc.getDescription();
		log.info("classify text" + text);
		
	
	return nerservice.tagSentences(text);
	}
	
	@POST
	@Path("/list")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "listing from document", notes = "listing entities from given document")
	public List<Term> list_post(Document doc) throws IOException  {
		String text = doc.getDescription();
		log.info("entities detect : " + text);
		
	
	return nerservice.list(text);
	}
	
	@GET
	@Path("/list")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "listing from text", notes = "listing entities from given text")
	public List<Term> list_get(@QueryParam("text") String text) throws IOException  {
		
		log.info("entities detect : " + text);
		
	
	return nerservice.list(text);
	}
	
}
