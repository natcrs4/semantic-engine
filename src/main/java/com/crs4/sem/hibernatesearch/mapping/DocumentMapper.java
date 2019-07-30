package com.crs4.sem.hibernatesearch.mapping;

import java.lang.annotation.ElementType;

import org.hibernate.search.annotations.Store;
import org.hibernate.search.cfg.SearchMapping;

import com.crs4.sem.model.Documentable;

public class DocumentMapper {
	
	public SearchMapping mapper() {
		SearchMapping mapping = new SearchMapping();

		mapping.entity(Documentable.class).indexed()
		       
		           .property("id", ElementType.FIELD) //field access
		               .documentId()
		                   .name("id")
		            .property("url", ElementType.METHOD)
		              .field()
		                .store(Store.YES);
		              
		return mapping;
	}

}
