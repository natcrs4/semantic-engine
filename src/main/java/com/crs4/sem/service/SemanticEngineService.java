package com.crs4.sem.service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.cfg.Configuration;
import org.neo4j.graphalgo.api.Graph;
import org.neo4j.graphalgo.core.GraphLoader;
import org.neo4j.graphalgo.core.huge.HugeGraphFactory;
import org.neo4j.graphalgo.impl.PageRankAlgorithm;
import org.neo4j.graphalgo.impl.PageRankResult;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.kernel.internal.GraphDatabaseAPI;

import com.crs4.sem.config.SemEngineConfig;
import com.crs4.sem.exceptions.NotUniqueDocumentException;
import com.crs4.sem.model.Document;
import com.crs4.sem.model.Documentable;
import com.crs4.sem.model.NewDocument;
import com.crs4.sem.model.NewSearchResult;
import com.crs4.sem.model.SearchResult;
import com.crs4.sem.model.Term;
import com.crs4.sem.neo4j.model.MyLabels;
import com.crs4.sem.neo4j.model.RRelationShipType;
import com.crs4.sem.neo4j.service.NodeService;
import com.mfl.sem.classifier.text.TextClassifier;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import lombok.Data;

@Data
public class SemanticEngineService {
	
	private NewDocumentService documentService;
	private NERService nerService;
	private NodeService nodeService;
	private PageRankResult pageRankResult;
	private TextClassifier textClassifier;                                 
	
	public static SemanticEngineService instance;
	public static SemanticEngineService newInstance(SemEngineConfig configuration) throws IOException {
		if(instance==null)
			instance=new SemanticEngineService(configuration);
		return instance;
	}
	
	
	public SemanticEngineService() {
		
	}
	public void addDocument(NewDocument doc) throws Exception  {
		
		List<Term> entities_title = nerService.listOfPerson(doc.getTitle());
		List<Term> entities_description = nerService.listOfPerson(doc.getDescription());
		Set<Term> entities= new HashSet<Term>();
		entities.addAll(entities_title);
		entities.addAll(entities_description);
	    
		Node nodedoc = nodeService.createNode(MyLabels.DOCUMENT.toString(), "title", doc.getTitle());
		doc.setEntities(toArrayString(entities));
		doc.setNeoid(nodedoc.getId());
		 documentService.saveOrUpdateDocument(doc);
		nodeService.setProperty(nodedoc,"docid",doc.getId());
		for( Term x:entities)
		{
			Node nodeentity=nodeService.searchNode(MyLabels.KEYWORD.toString(), "forma", x.content());
			if(nodeentity==null)
				nodeentity=nodeService.createNode(MyLabels.KEYWORD.toString(), "forma", x.content());
			nodeService.addRelationShip(nodedoc, nodeentity, RRelationShipType.CONTAINS);
		}
		
		
	}
	public void addAll(List<NewDocument> documents) throws Exception  {
		for(NewDocument doc:documents) {
			this.addDocument(doc);
		}
	}
	private String[] toArrayString(Set<Term> entities) {
		 String [] result= new String[entities.size()];
	 int i=0;
		for(Term x:entities){
			result[i]=x.toString();
			i++;
		}
			
		return result;
	}

	public NewSearchResult semanticSearch(String text, int start, int maxresults){
		return documentService.semanticSearch(text, this.getPageRankResult(), start, maxresults);
	}
	
	public PageRankResult  pageRank(){
		Graph graph = new GraphLoader((GraphDatabaseAPI)this.getNodeService().getGraphDb())
                .withDirection(Direction.BOTH)
                .load(HugeGraphFactory.class);

		PageRankResult result = PageRankAlgorithm.of(graph, 0.85)
        .compute(5)
        .result();
		this.setPageRankResult(result);
		return result;
}



	public SemanticEngineService(SemEngineConfig config) throws IOException {
		 String path=  config.getHibernateSemDocuments();
		 if (path.startsWith("classpath:")) {
		    	path=path.replace("classpath:", config.classpath()+"/applications/"+config.applicationame()+"/WEB-INF/classes/");
		    }
		    Configuration configure = HibernateConfigurationFactory.configureDocumentService(new File(path));
			NewDocumentService docservice= new NewDocumentService(configure);
		this.setDocumentService(docservice);
		File file = new File(config.icabparameters());
		this.setNerService(new NERService(file));
		GraphDatabaseService graph = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File(config.semneo4j()))
		.setConfig(GraphDatabaseSettings.pagecache_memory, "512M")
		.setConfig(GraphDatabaseSettings.string_block_size, "60")
		.setConfig(GraphDatabaseSettings.array_block_size, "300")
		.setConfig(GraphDatabaseSettings.read_only,"false").newGraphDatabase();
		this.setNodeService(new NodeService(graph));
		
	}

	public SemanticEngineService(NodeService node,NERService nerservice, NewDocumentService docservice) {
		this.setNerService(nerservice);
		this.setDocumentService(docservice);
		this.setNodeService(node);
	}

	public Documentable get(Long id,Boolean link) {
	    Documentable doc=this.getDocumentService().getNaturald(id,link);
		return doc;
	}
	
	
	public void buildGraph() {
	
				
				
				String taxo[]={"esteri","economia","cronache","tecnologia","politica","mare","montagna","sport","cultura"};
				long id=0L;
				for (String cat:taxo){
					try {
				URL feedSource = new URL("http://feed.lastampa.it/"+cat+".rss");
				SyndFeedInput input = new SyndFeedInput();
				SyndFeed feed;
			
					feed = input.build(new XmlReader(feedSource));
					String desc=feed.getDescription();
					List<SyndEntry> entries = (List<SyndEntry>)feed.getEntries();
				    for (SyndEntry entry: entries){
				    	  System.out.println("Title: " + entry.getTitle());
			        System.out.println("Link: " + entry.getLink());
			        System.out.println("Author: " + entry.getAuthor());
			        System.out.println("Publish Date: " + entry.getPublishedDate());
			        System.out.println("Description: " + entry.getDescription().getValue());
			        id++;
			       NewDocument document=NewDocument.builder().internal_id(DigestUtils.md5Hex(entry.getLink())).id(id).authors(entry.getAuthor()).description(entry.getDescription().getValue()).url(entry.getLink()).title(entry.getTitle()).publishDate(entry.getPublishedDate()).build();
			       document.setId(NewDocument.setHashID(document.getInternal_id().getBytes()));
			       this.addDocument(document);
				    } }catch (IllegalArgumentException | FeedException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
			   
			}
					
					this.pageRank();
	}
	public void shutdown() {
		this.getDocumentService().close();
		this.getNodeService().getGraphDb().shutdown();
		
	}
}
