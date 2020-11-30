package dk.kvalitetsit.cda;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;

import dk.kvalitetsit.cda.configuration.ApplicationConfiguration;
import dk.kvalitetsit.cda.configuration.DgwsConfiguration;
import dk.kvalitetsit.cda.configuration.PatientContext;
import dk.kvalitetsit.cda.document.DocumentHelperPHMRImpl;
import dk.kvalitetsit.cda.document.DocumentHelperQFDDImpl;
import dk.kvalitetsit.cda.services.CdaMetaDataFactory;
import dk.kvalitetsit.cda.services.DocumentProcessor;
import dk.kvalitetsit.cda.services.XdsRequestService;


@Import({ApplicationConfiguration.class,  DgwsConfiguration.class})
@EnableAutoConfiguration
public class Application implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
	
	@Autowired
	XdsRequestService xdsRequestService;
	
	@Autowired
	PatientContext userContext;
	
	@Autowired
	CdaMetaDataFactory cdaMetaDataFactory;

	@Autowired
	DocumentProcessor documentProcessor;

	
	public static void main(String[] args) throws Exception {
		LOGGER.debug("Starting application");
		SpringApplicationBuilder sab = new SpringApplicationBuilder(Application.class);
		sab.web(false);
		sab.run(args);
	}	

	public void run(String... args) throws Exception {
		
		// To add a new type of document add a new DocumentHelper and DocumentFactory and start activating it below.

	    if (true) {
			//PHMR = Personal Health Monitoring Report
			documentProcessor.runCDADocument(new DocumentHelperPHMRImpl());	    	
	    }
	    
	    if (false) { //TODO: Currently Open XDS does not seem to like the QFDD format.
			//QFDD = Questionnaire Form Definition Document
			documentProcessor.runCDADocument(new DocumentHelperQFDDImpl());	    	
	    }

	}
	

}
