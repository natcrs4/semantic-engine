package com.crs4.sem.server;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.crs4.sem.jaxrs.application.Application;

import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URI;

import javax.persistence.Persistence;



public class MainTest {
    // logger
    private static final Logger logger = LoggerFactory.getLogger(MainTest.class);

    // Dropwizard Metrics
    public static final MetricRegistry METRIC_REGISTRY = new MetricRegistry();

    // Base URI parameters the Grizzly HTTP server will listen on
    private static String host;
    private static String port;

    public static String getBaseUri()
    {
        return String.format("http://%s:%s/semantic-engine/rest/", host, port);
    }

    public static String getHost() {
        return host;
    }

    public static String getPort() {
        return port;
    }

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        // create a resource config that scans for JAX-RS resources and providers
        // in com.example.test package
         Application app = new Application("com.crs4.sem.rest", getHost(), getPort());

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(getBaseUri()), app);
    }

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
   
	public static void main(String [] args ) throws IOException {
		
        host = System.getProperty("host.ip", "0.0.0.0");
        port = System.getProperty("host.port", "8085");
        System.getProperty("sem.engine.basedire","/Users/mariolocci/Documents/workspace-tmp/semengine_conf");
        final HttpServer server = startServer();
      
        final JmxReporter reporter = JmxReporter.forRegistry(METRIC_REGISTRY).build();
        reporter.start();
        logger.info("........");
        //CLStaticHttpHandler staticHttpHandler = new CLStaticHttpHandler(MainTest.class.getClassLoader(), "swagger-ui/");
       // server.getServerConfiguration().addHttpHandler(staticHttpHandler, "/docs");
       
        logger.info(getBaseUri());

       // Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        Thread.currentThread();
    }
}

