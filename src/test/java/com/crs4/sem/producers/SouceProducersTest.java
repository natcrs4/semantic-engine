package com.crs4.sem.producers;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.aeonbits.owner.ConfigFactory;
import org.junit.Test;

import com.crs4.sem.config.SemEngineConfig;

public class SouceProducersTest {
	
@Test
public void produces() {
	System.setProperty("sem.engine.basedirectory", "/Users/mariolocci/Documents/workspace-tmp/semengine_conf");
	SemEngineConfig config = ConfigFactory.create(SemEngineConfig.class,System.getProperties());
	SourceProducer producer = new SourceProducer();
	producer.setConfig(config);
	Map<String, String> map = producer.produces();
	assertEquals(map.get("1"),"Economia");
}

}
