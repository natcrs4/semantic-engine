package com.crs4.sem.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PairStringInteger implements Comparable<PairStringInteger>{
	
	private Integer f;
	private String key;
	@Override
	public int compareTo(PairStringInteger o) {
		return o.getF().compareTo(this.getF());
		
	}

}
