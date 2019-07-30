package com.crs4.sem.rest;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.crs4.sem.model.Document;
import com.crs4.sem.model.Link;
import com.crs4.sem.model.Shado;
import com.crs4.sem.producers.DocumentProducerType;
import com.crs4.sem.producers.ServiceType;
import com.crs4.sem.service.AuthorService;
import com.crs4.sem.service.ShadoService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.Data;

//@Stateless
@ApplicationScoped
@Path("/shado")
@Api(value = "Shado", description = "Shado")
@Data
public class ShadoRestResources {

	@Inject
	 @DocumentProducerType(ServiceType.SHADO)
	private ShadoService shadoService;
	
	@Inject
	private Logger log;

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get remap of a document id", notes = "get the remap of a document")
	public Shado getShado(@PathParam("id") String id)  {

		log.info("get shado record with internal id " + id);
		return this.shadoService.getShado(id);
	}

	
	@GET
	@Path("/link/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get link", notes = "get link")
	public Link getLink(@PathParam("id") String id)  {

		log.info("get shado record with internal id " + id);
		return this.shadoService.getLink(id);
	}
	@POST
	@Path("/add")
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "add shado links", notes = "Add a list of shado links")
	public Response addDocument(List<Shado> shadows) {

		log.info("add shado links");
		this.shadoService.setMD5id(shadows);
		shadows=this.shadoService.checkLinks(shadows);
Set<Shado> shadoset= new HashSet<Shado>();
shadoset.addAll(shadows);
		this.shadoService.addAll(shadoset);
		log.info("added " + shadows.size()+ " shado links");
		return Response.ok().build();

	}
	
	@GET
	@Path("/size")
	@Produces(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Get size ", notes = "get size")
	public Long size() throws IOException {

		Long size = this.shadoService.size();
		log.info("getting size " + size);
		return size;

	}
	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	//@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "delete shado document ", notes = "Delete shado document from  database ")
	public Response deleteShado(@ApiParam(value = "internal id" )@PathParam("id") String id) {
		Shado doc=this.shadoService.deleteDocument(id);
		if(doc==null) return Response.status(Status.NOT_FOUND).build();
		return Response.ok().build();
	}
}
