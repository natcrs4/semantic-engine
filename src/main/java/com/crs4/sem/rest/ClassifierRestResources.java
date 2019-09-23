package com.crs4.sem.rest;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.lucene.analysis.Analyzer;
import org.neo4j.graphdb.Node;


import com.crs4.sem.model.Documentable;
import com.crs4.sem.model.NewDocument;
import com.crs4.sem.neo4j.exceptions.CategoryNotFoundInTaxonomyException;
import com.crs4.sem.neo4j.service.TaxonomyService;
import com.crs4.sem.producers.AnalyzerType;
import com.crs4.sem.producers.Analyzers;
import com.crs4.sem.producers.DocumentProducerType;
import com.crs4.sem.producers.ServiceType;
import com.crs4.sem.service.NewDocumentService;
import com.mfl.sem.classifier.HClassifier;
import com.mfl.sem.classifier.HClassifierBuilder;
import com.mfl.sem.classifier.exception.ClassifierException;
import com.mfl.sem.classifier.impl.SVMClassifier;
import com.mfl.sem.classifier.model.CategoryDictionary;
import com.mfl.sem.classifier.model.Dictionary;
import com.mfl.sem.classifier.text.Documents;
import com.mfl.sem.classifier.text.TextClassifier;
import com.mfl.sem.classifier.text.impl.TextClassifierImpl;
import com.mfl.sem.dataset.reader.DocumentReader;
import com.mfl.sem.model.ScoredItem;
import com.mfl.sem.text.model.Doc;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.Data;

@Data
@Stateless
//@ApplicationScoped
@Path("/classifier")
@Api(value = "Classifier", description = "Resource api for classifing text")
public class ClassifierRestResources {
@Inject
TextClassifier textClassifier;

@Inject
Logger log;
@Inject
@AnalyzerType(Analyzers.ITALIAN)
Analyzer analyzer;




@Inject 
TaxonomyService taxonomyService;

@Inject 
@DocumentProducerType(ServiceType.DOCUMENT)
NewDocumentService documentService;

@GET
@Path("/classifyText")
@Produces(MediaType.APPLICATION_JSON)
@ApiOperation(value = "classify text", notes = "Method for classifying text", response = List.class)
@ApiResponses(value = {
        @ApiResponse(code = 200, message = "Got it"),
        @ApiResponse(code = 500, message = "Server is down!")
})
public List<ScoredItem> classifyText(@QueryParam("text") String text) throws IOException, ParseException {
	log.info("classify document" + text);
	Doc doc= new Doc();
	doc.setDescription(text);
    List<ScoredItem> list = this.getTextClassifier().classify(doc);
	return list;
}

@POST
@Path("/classifyText")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApiOperation(value = "classify text", notes = "Classify text")
public List<ScoredItem> classifyDoc(String text) throws IOException, InterruptedException {

	log.info("classify document" + text);
	Doc doc= new Doc();
	doc.setDescription(text);
	return this.getTextClassifier().classify(doc);
	
}
@POST
@Path("/classifyDocument")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApiOperation(value = "classify document", notes = "Classify a single document")
public List<ScoredItem> classifyDoc( NewDocument doc) throws IOException, InterruptedException {

	log.info("classify document" + doc);
	
	return this.getTextClassifier().classify(doc);
	
}

@GET
@Path("/train/{name}")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.APPLICATION_JSON)
@ApiOperation(value = "classify document", notes = "Train classifier")
public Response train(@PathParam("name") String stringroot) throws IOException, InterruptedException, CategoryNotFoundInTaxonomyException, InstantiationException, IllegalAccessException, ClassifierException {

	log.info("train classifier " );
	//aux=resourceContext.getResource(TextClassifier.class);

	CategoryDictionary categoryDictionary = new CategoryDictionary();
	Node root = taxonomyService.searchCategory(stringroot);
	HClassifier<SVMClassifier> hclassifier = HClassifierBuilder.builder().species(SVMClassifier.class).root(root)
			.taxonomyService(taxonomyService).categoryBuilder(categoryDictionary).build();
	//SVMClassifier svm = new SVMClassifier();
	TextClassifier textClassifier = new TextClassifierImpl(analyzer,hclassifier,categoryDictionary);
	List<Documentable> docs= new ArrayList<Documentable>();
	List<Documentable> docs_ = documentService.getTrainable();
	String[] categories = taxonomyService.branchLabels(root, false);
	List<Documentable> kdocs = new ArrayList<Documentable>();
	
	
	for (String category : categories) {
		String[] keywords;
		keywords = taxonomyService.getKetwords("root", category);
		//if(category.trim().length()==0) System.out.println("_____");
		String[] categories_ = new String[1];
		categories_[0] = category;
		if(keywords!=null) 
		for (int i = 0; i < keywords.length; i++) {
			Documentable doc = NewDocument.builder().title(keywords[i]).categories(categories_).build();
            kdocs.add(doc);
		}
	}
	docs.addAll(kdocs);
	if(!docs_.isEmpty())docs.addAll(docs_);
	Documents kdocsreader = new DocumentReader(docs);
	TextClassifierImpl aux = (TextClassifierImpl) textClassifier;
	aux.setCategoryDictionary(new CategoryDictionary());
	aux.setDictionary(new Dictionary());
	aux.setClassifier(hclassifier);
	
   
	 try {
		this.getTextClassifier().train(kdocsreader);
		return Response.ok("classifier trained").build();
	} 
	 catch (ClassifierException e) {
		log.log(Level.SEVERE, e.getMessage());
	    return Response.serverError().build();
		
	}
	 
}

@GET
@Path("/classifyDocuments/{name}")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.APPLICATION_JSON)
@ApiOperation(value = "Classifies all documents", notes = "Classifies all documents")
public Response classifyDocuments(@PathParam("name") String stringroot) {
	this.documentService.optimizeIndex();
	Long num = this.documentService.classifyAll(this.getTextClassifier());
	return Response.ok("classified "+num+" documents").build();
}

}
