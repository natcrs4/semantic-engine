package com.crs4.sem.service.scheduled;

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.ejb.TimerService;
import javax.inject.Inject;

import com.crs4.sem.model.NewDocument;
import com.crs4.sem.producers.DocumentProducerType;
import com.crs4.sem.producers.ServiceType;
import com.crs4.sem.service.AuthorService;
import com.crs4.sem.service.NewDocumentService;

import lombok.Data;


@Stateless
@Data
public class ScheduledMaintenanceService {
	
	private Integer start=0;
	static public Integer maxresults=100; 
	
	
	@Inject
	@DocumentProducerType(ServiceType.DOCUMENT)
	private NewDocumentService documentService;


	@Resource
	private TimerService timerservice;
	
	@Inject
	private Logger log;
	
	

	@Inject
	@DocumentProducerType(ServiceType.AUTHORS)
    private AuthorService authorService;

	
	//@Schedule( hour="*",minute="*/5", persistent=false)
	public void cleansReplicas() {
		log.info("starting clean replica process");
		
		Set<NewDocument> docu = this.documentService.getReplicas(start, maxresults);
		log.info(" found " + docu.size() + " documents to delete");
		this.documentService.deleteAll(new ArrayList<NewDocument>(docu));
		start=start+maxresults;
		
	}
	
	@Schedule(dayOfWeek="sun", hour="0",minute="0", persistent=false)
	public void buildAuthor() {
		log.info("starting build authors process");
		this.documentService.buildAuthors(authorService);
		log.info("end build authors process");
	}

}
