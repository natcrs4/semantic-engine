package com.crs4.sem.model;

import javax.persistence.Lob;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Metadata {
@Lob
private String url;
private Float duration;
@Lob
private String title;
@Lob
private String description;
@Lob 
private String poster;
}
