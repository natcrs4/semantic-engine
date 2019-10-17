package com.crs4.sem.model;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import lombok.Data;

@Singleton
@Data
public class StatusSingleton {
 
   private boolean allowadd;
   
   @PostConstruct
   public void initialize() {
	   this.setAllowadd(true);
   }
}
