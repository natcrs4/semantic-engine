package com.crs4.sem.rest;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.crs4.sem.model.Document;
import com.crs4.sem.model.Documentable;
import com.crs4.sem.model.NewDocument;
import com.crs4.sem.model.NewSearchResult;
import com.crs4.sem.model.SearchResult;
import com.crs4.sem.producers.DocumentProducerType;
import com.crs4.sem.producers.ServiceType;
import com.crs4.sem.service.DocumentService;
import com.crs4.sem.service.NERService;
import com.crs4.sem.service.SemanticEngineService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

@Stateless
//@ApplicationScoped
@Path("/semantics")
@Api(value = "Semantics", description = "Resources api for semantic processing of documents")
@Data
public class SemanticEngineRestResources {
	
	@Inject
	@DocumentProducerType(ServiceType.SEMANTICS)
	private SemanticEngineService documentService;

	@Inject
	private Logger log;

	

	
	
	@GET
	@Path("/search")
	@ApiOperation(value = "semantic search",
    notes = "Search document from a store matching text",
    response = SearchResult.class)
	@Produces(MediaType.APPLICATION_JSON)
	public NewSearchResult searchText(@QueryParam("text") String text,@QueryParam("entities") @DefaultValue("false") Boolean entities,@QueryParam("start") @DefaultValue("0") Integer start,@QueryParam("maxresults") @DefaultValue("1000") Integer maxresults, @QueryParam("from") String from, @QueryParam("to") String to) throws IOException, ParseException {

		log.info("classify text" + text);
		Date from_date=null;
		Date to_date=null;
			SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd" );
		if(from!=null) 
			from_date=df.parse(from);
		if(to!=null) 
			to_date=df.parse(to);
		NewSearchResult searchResult = this.documentService.semanticSearch(text,start,maxresults);
		
		return searchResult;
	}
	
	@GET
	@Path("/similars/{id}")
	@ApiOperation(value = "get similars",
    notes = "Search similar documents to document id",
    response = Document.class)
	@Produces(MediaType.APPLICATION_JSON)
	public List<NewDocument> similars(@PathParam("id") Long id,@QueryParam("start") @DefaultValue("0") Integer start,@QueryParam("maxresults") @DefaultValue("1000") Integer maxresults) throws IOException, ParseException {

		log.info("searching similars to document id :" + id);
	    Documentable doc=this.documentService.get(id,false);
	  //  doc.get
		//List<Document> docs = this.documentService.semanticSearch(doc,start,maxresults);
	    List<NewDocument> docs=null;
		return docs;
	}

	
	@GET
	@Path("/buildgraph")
	@ApiOperation(value = "build entity graph",
    notes = "Build graph using entities")
	public Response buildGraph() {
		this.documentService.buildGraph();
		return Response.ok().build();
	}
}
