package com.crs4.sem.commandline;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.h2.tools.Recover;
import org.h2.tools.RunScript;
import org.hibernate.cfg.Configuration;

import com.crs4.sem.neo4j.exceptions.CategoryNotFoundInTaxonomyException;
import com.crs4.sem.neo4j.exceptions.TaxonomyNotFoundException;
import com.crs4.sem.service.DocumentService;
import com.crs4.sem.service.HibernateConfigurationFactory;
import com.crs4.sem.service.NewDocumentService;
import com.crs4.sem.service.ShadoService;

public class NewCommandLine {
	
	public static void main(String args[]) throws InterruptedException, SQLException  {
		Options options = new Options();
		
		Option  reindex = Option.builder().longOpt("reindex").argName("reindex").desc("reindex, needs hibernate configuration").numberOfArgs(1).build();
		Option recover = Option.builder().longOpt("recover").argName("recover").desc("recover h2 db").numberOfArgs(1).build();
		Option runscript = Option.builder().longOpt("runscript").argName("runscript").desc("recover h2 db").numberOfArgs(1).build();
		Option h2tomysqlshado = Option.builder().longOpt("h2tomysqlshado").argName("h2tomysqlshado").desc("recover h2 db").numberOfArgs(2).build();
		options.addOption(reindex);
		options.addOption(recover);
		options.addOption(runscript);
		options.addOption(h2tomysqlshado);
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("utility-name", options);

			System.exit(1);
		}
		if (cmd.hasOption("reindex")) {
			String value = cmd.getOptionValue("reindex");
			File cfgFileh2 = new File(value);
			Configuration configureh2 = HibernateConfigurationFactory.configureDocumentService(cfgFileh2);
			NewDocumentService docservice = new NewDocumentService(configureh2);
			docservice.rebuildIndex();
		}
		if (cmd.hasOption("recover")) {
			String values[] = cmd.getOptionValues("recover");
			Recover rec = new Recover();
			String name=values[0].replaceAll(".h2.db","");
			
			String comm="-dir " + values[0]+" -db "+name;
		    rec.runTool(comm.split(" "));
			
		}
		if (cmd.hasOption("runscript")) {
			String values[] = cmd.getOptionValues("runscript");
			String name=values[0];

			String comm="-url jdbc:h2:./"+"recovered"+" -continueOnError -user sa -script " +name;
					
			String arg[]=comm.split(" ");
			RunScript.main(arg);
			
		}
		if (cmd.hasOption("h2tomysqlshado")) {
			String[] values = cmd.getOptionValues("h2tomysqlshado");
			File cfgFile1= new File(values[0]);
			
			Configuration configure1= HibernateConfigurationFactory.configureShadoService(cfgFile1);
			ShadoService source= new ShadoService(configure1);
			File cfgFile2= new File(values[1]);
			Configuration configure2 = HibernateConfigurationFactory.configureShadoService(cfgFile2);
			ShadoService destination = new ShadoService(configure2);
			source.migrate(destination);
            System.exit(0);
		}
	}
}
