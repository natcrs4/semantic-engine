package com.crs4.sem.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Key;
import org.aeonbits.owner.Config.Sources;

@Sources({"classpath:semengine.properties","file:${sem.engine.basedirectory}/semengine.properties","file:conf.properties"})
public interface SemEngineConfig extends Config {

	
	@Key("sem.engine.neo4j.graph.directory")
	public String neo4jDirectory();
	
	@Key("sem.engine.neo4j.graph.read_only")
	public String neo4jreadonly();
	
    @Key("com.sun.aas.instanceRoot")	
	public String classpath();

	@Key("sem.engine.tnttagger.icab.paramenters")
	public String icabparameters();
	@Key("sem.engine.host")
	public String host();
	
	@Key("sem.engine.port")
	public String port();
	
   @DefaultValue("semantic-engine")	
	@Key("sem.engine.application.name")
	public String applicationame();

	@Key("sem.engine.taxocsv")
	public String taxonomyCsv();

	@Key("sem.engine.source.categorizations")
	public String getCategorizedSources();

	@Key("sem.engine.source.identifiers")
	public String getSourceIdentifiers();

	@Key("sem.engine.documents.adding")
	public boolean addingdocs();

	@Key("sem.engine.hibernate.cfg.authors")
	public String getHibernateCFGAuthors();

	@Key("sem.engine.hibernate.cfg.documents")
	public String getHibernateCFGDocuments();
	
	@Key("sem.engine.hibernate.sem.documents")
	public String getHibernateSemDocuments();

	@Key("sem.engine.hibernate.cfg.shado")
	public String getHibernateCFGShado();
	
	@Key("sem.engine.hibernate.search.indexbase")
	public String indexbase();

	@Key("sem.engine.neo4j.sem.directory")
	public String semneo4j();
	
}
