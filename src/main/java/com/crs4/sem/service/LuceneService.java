package com.crs4.sem.service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.store.NoLockFactory;

import com.crs4.sem.convertes.LuceneDocumentConverter;
import com.crs4.sem.lucene.similarity.ScoreRankScoreQuery;
import com.crs4.sem.model.NewDocument;
import com.crs4.sem.model.NewMetadata;
import com.crs4.sem.model.NewSearchResult;
import com.crs4.sem.model.Page;
import com.crs4.sem.model.lucene.EntityDocumentBuilder;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LuceneService {
	public static final Logger logger = Logger.getLogger(NewDocumentService.class);
	

		public  IndexReader indexReader ;
		public  IndexSearcher indexSearcher ;
		public static LuceneService instance;
		public LuceneService(String source) throws IOException {
		Path path = Paths.get(source);
		Directory directory = FSDirectory.open(path, NoLockFactory.INSTANCE);
	      indexReader = DirectoryReader.open(directory);
	      indexSearcher = new IndexSearcher(indexReader);
		}
		
		
		public static LuceneService newInstance(String source) throws IOException {
			if(instance==null)
				instance=new LuceneService(source);
			return instance;
		}
		public NewSearchResult parseSearch(String text, String query, Date from, Date to, int start, int maxresults,
				boolean score, Analyzer analyzer,Boolean links) throws Exception {
			BooleanClause.Occur[] flags = { BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD,
					BooleanClause.Occur.SHOULD};
			String fields[] = { "url", "title", "description"};
			BooleanClause.Occur[] flags_ = {};
			String fields_[] = { "id", "url", "authors", "type", "source_id", "internal_id", "publishDate", "links",
					"movies", "gallery", "attachments", "podcasts", "score", "entities", "trainable" };
		
		
			List<NewDocument> result = new ArrayList<NewDocument>();
			
			
	

			
				if(!text.isEmpty())
					{
					  text=" (url: "+text+ " )"+" (title: "+text+ " )"+" (description: "+text+ " )";
					  query= text+ " " + query;
					}
				    
				Query luceneQuery=null;
				if (!query.trim().isEmpty()) {
					// luceneQuery = queryparser.parse(query);
					QueryParser queryparser = new QueryParser("", analyzer);
					luceneQuery = queryparser.parse(query);
				}
			
				 Query resultQuery =luceneQuery;
				if (from != null || to != null) {
					if (to == null)
						to = Calendar.getInstance().getTime();
					if (from == null) {
						Calendar cal = Calendar.getInstance();
						cal.add(Calendar.YEAR, -1);
						from = cal.getTime();
					}
					Query rangeQuery = NumericRangeQuery.newLongRange("publishDate", from.getTime(), to.getTime(), true, true);//.newStringRange("publishData", from.getTime()+"", to.getTime()+"", true, true);
				  
					if(luceneQuery!=null) {
					resultQuery = new BooleanQuery.Builder().add(rangeQuery, BooleanClause.Occur.MUST)
							.add(luceneQuery, Occur.MUST).build();
					}
					else 
						resultQuery=rangeQuery;
					
				}
				logger.info("query "+resultQuery);
				if (score)
					resultQuery = new ScoreRankScoreQuery(resultQuery);
				// FullTextQuery fullTextQuery = fts.createFullTextQuery(customQuery,
				// Document.class);
				//FullTextQuery fullTextQuery = fts.createFullTextQuery(resultquery, NewDocument.class);
				//fullTextQuery.setFirstResult(start);
				//fullTextQuery.setMaxResults(maxresults);

				//collector = new TopDocsCollector(maxresults);
				TopDocs docs = indexSearcher.search(resultQuery, start+maxresults);
				ScoreDoc[] hits = docs.scoreDocs;
				   int max = Math.min(start+maxresults, docs.totalHits);
				 int totaldocs= docs.totalHits;
				 List<Document> documents= new ArrayList<Document>();
						for(int i = start; i < max; i++) 
						  { 
							Document document = indexSearcher.doc(hits[i].doc);
						
							documents.add(document);
						  }
						  result= new ArrayList<NewDocument>();
						Map<String,NewMetadata> metadatas= new HashMap<String,NewMetadata>();
						Map<String,Page> pages= new HashMap<String,Page>();
						for(org.apache.lucene.document.Document document:documents) {
						NewDocument doc = LuceneDocumentConverter.convertToNewDocument(document);
						result.add(doc);
						}

			NewSearchResult searchResult = NewSearchResult.builder().documents(result).totaldocs(totaldocs).build();
			return searchResult;
		}

		public static String signature(Document document) {
			// TODO Auto-generated method stub
			return null;
		}
}
