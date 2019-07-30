package com.crs4.sem.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.crs4.sem.model.Shado.ShadoBuilder;
import com.ibm.icu.impl.Pair;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table( name="LINKS" )
public class Link {
	
	@Id
	private String id;
	
	
    @Lob 
    @EqualsAndHashCode.Exclude
    private String link;
	
    @Lob
    @EqualsAndHashCode.Exclude
    private String newLink;
    
    @Embedded
    @EqualsAndHashCode.Exclude
    private Metadata metadata;
}
