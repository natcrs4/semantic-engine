package com.crs4.sem.rest.deserializer;
import java.io.IOException;

import com.crs4.sem.model.Link;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
public class LinkDeserializer {//extends StdDeserializer<Link>{

	
//	@Override
//	public Link deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
//		 JsonNode node = jp.getCodec().readTree(jp);
//	        int id = (Integer) ((IntNode) node.get("id")).numberValue();
//	        String itemName = node.get("itemName").asText();
//	        int userId = (Integer) ((IntNode) node.get("createdBy")).numberValue();
//	 
//	        return new Item(id, itemName, new User(userId, null));
//	}

}
