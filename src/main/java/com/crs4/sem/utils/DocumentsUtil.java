package com.crs4.sem.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Node;

import com.crs4.sem.model.Documentable;
import com.crs4.sem.model.NewDocument;
import com.crs4.sem.neo4j.exceptions.CategoryNotFoundInTaxonomyException;
import com.crs4.sem.neo4j.exceptions.TaxonomyNotFoundException;
import com.crs4.sem.neo4j.service.TaxonomyService;
import com.crs4.sem.service.NewDocumentService;
import com.mfl.sem.text.model.Doc;

public class DocumentsUtil {
	 public static List<Documentable> expandUniLabel(List<Documentable> documents) {
			
			List<Documentable> docs= new ArrayList<Documentable>();
		
			for(Documentable doc:documents) {
		
				for(String cat:doc.getCategories()) {
					String []newcategories= new String[1];
					newcategories[0]=cat;
					Doc newdoc = Doc.builder().authors(doc.getAuthors()).url(doc.getUrl()).description(doc.getDescription()).title(doc.getTitle()).categories(newcategories).build();
				    docs.add(newdoc);
				}}
			return docs;
		}
	 
	  public static void parentize(List<Documentable> documents, TaxonomyService service) {
		  for(Documentable doc:documents) {
			  String[] categories = doc.getCategories();
			  String[] parents= new String[categories.length];
			  int i=0;
			  for(String cat:categories) {
				  System.out.println(cat);
				  if(service.isLeaf(cat)) {
					 
					  parents[i]=service.getParent(cat);
				  }
				  else parents[i]=categories[i];
				  i++;
			  }
			  doc.setCategories(parents);
		  }
		  
	  }
	  public static List<NewDocument> getAllTrainableDocument(TaxonomyService taxonomyService,NewDocumentService documentService, String taxoname, boolean links) throws TaxonomyNotFoundException, CategoryNotFoundInTaxonomyException {
		  Node root=taxonomyService.searchCategory(taxoname);
		  List<NewDocument> list= new ArrayList<NewDocument>();
			Map<String,Set<String>> mapdoc= new HashMap<String,Set<String>>();
			if(root==null) throw new com.crs4.sem.neo4j.exceptions.TaxonomyNotFoundException();
		    String[] labels = taxonomyService.branchLabels(root, true);
		    Set<String> documents= new HashSet<String>();
		    for( String label:labels) {
		    	
		        String[] klist = (taxonomyService.getDocuments(taxoname, label));
		 
		    	for(String id:klist) {
		    		Set<String> set = mapdoc.get(id);
		    		if(set==null) {
		    			set=new HashSet<String>();
		    			mapdoc.put(id,set);
		    		}
		    		set.add(label);
		    		documents.add(id);
		    	}
		    	
		    }
		    for(String id:mapdoc.keySet()) {
		    	NewDocument aux=documentService.getById(id, links);
		    	aux.setTrainable(true);
			  list.add(aux);
		    }
		documentService.checkDocuments(list);
		
		
		 
		 return list;
	  }

}
