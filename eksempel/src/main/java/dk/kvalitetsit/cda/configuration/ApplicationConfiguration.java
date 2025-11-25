package dk.kvalitetsit.cda.configuration;

import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.transport.http.HTTPConduit;
import org.openehealth.ipf.commons.ihe.ws.WsTransactionConfiguration;
import org.openehealth.ipf.commons.ihe.xds.core.XdsClientFactory;
import org.openehealth.ipf.commons.ihe.xds.iti18.Iti18PortType;
import org.openehealth.ipf.commons.ihe.xds.iti41.Iti41PortType;
import org.openehealth.ipf.commons.ihe.xds.iti43.Iti43PortType;
import org.openehealth.ipf.commons.ihe.xds.iti57.Iti57PortType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import dk.kvalitetsit.cda.services.CdaMetaDataFactory;
import dk.kvalitetsit.cda.services.DocumentProcessor;
import dk.kvalitetsit.cda.services.XdsRequestBuilderService;
import dk.kvalitetsit.cda.services.XdsRequestService;
import dk.kvalitetsit.cda.utilities.OrganisationIdAuthority;
import dk.kvalitetsit.cda.utilities.PatientIdAuthority;
import dk.s4.hl7.cda.codes.NSI;

public class ApplicationConfiguration {

	private static String DEFAULT_PATIENT_ID = "2512489996";

	private static Logger LOGGER = LoggerFactory.getLogger(ApplicationConfiguration.class);

	private TrustManager[] trustAllCerts = new TrustManager[] {
		       new X509TrustManager() {
		          public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		            return null;
		          }

		          public void checkClientTrusted(X509Certificate[] certs, String authType) {  }

		          public void checkServerTrusted(X509Certificate[] certs, String authType) {  }

		       }
		    };
	
	@Value("${xds.iti18.endpoint}")
	String xdsIti18Endpoint;

	@Value("${xds.iti18.wsdl}")
	String xdsIti18Wsdl;

	@Value("${xds.iti41.endpoint}")
	String xdsIti41Endpoint;

	@Value("${xds.iti41.wsdl}")
	String xdsIti41Wsdl;

	@Value("${xds.iti43.endpoint}")
	String xdsIti43Endpoint;

	@Value("${xds.iti43.wsdl}")
	String xdsIti43Wsdl;

	@Value("${xds.iti57.endpoint}")
	String xdsIti57Endpoint;

	@Value("${xds.iti57.wsdl}")
	String xdsIti57Wsdl;

	@Bean
	public PatientIdAuthority getPatientIdAuthority() {
		return new PatientIdAuthority(NSI.CPR_OID);
	}
	
	@Bean
	public PatientContext getUserContext() {
		PatientContext uc = new PatientContext(DEFAULT_PATIENT_ID);
		return uc;
	}

	@Bean
	public Iti18PortType getDocumentRegistryServiceIti18() {
		LOGGER.info("Creating Iti18PortType for url: "+xdsIti18Endpoint);

		XdsClientFactory xdsClientFactory = generateXdsRegistryClientFactory(xdsIti18Wsdl, xdsIti18Endpoint, Iti18PortType.class);
		Iti18PortType client = (Iti18PortType) xdsClientFactory.getClient();

		initProxy(client);
		setSoapActionHeader((BindingProvider) client, "urn:ihe:iti:2007:RegistryStoredQuery");

		return client;
	}

	@Bean
	public Iti41PortType getDocumentRepositoryServiceIti41() {
		LOGGER.info("Creating Iti41PortType for url: "+xdsIti41Endpoint);

		XdsClientFactory xdsClientFactory = generateXdsRepositoryClientFactory(xdsIti41Wsdl, xdsIti41Endpoint, Iti41PortType.class);
		Iti41PortType client = (Iti41PortType) xdsClientFactory.getClient();

		initProxy(client);
		setSoapActionHeader((BindingProvider) client, "urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-b");

		return client;
	}

	@Bean
	public Iti43PortType getDocumentRepositoryServiceIti43() {
		LOGGER.info("Creating Iti43PortType for url: "+xdsIti43Endpoint);

		XdsClientFactory xdsClientFactory = generateXdsRepositoryClientFactory(xdsIti43Wsdl, xdsIti43Endpoint, Iti43PortType.class);
		Iti43PortType client = (Iti43PortType) xdsClientFactory.getClient();
		
		initProxy(client);
		setSoapActionHeader((BindingProvider) client, "urn:ihe:iti:2007:RetrieveDocumentSet");

		return client;
	}

	@Bean
	public Iti57PortType getDocumentRepositoryServiceIti57() {
		LOGGER.info("Creating Iti57PortType for url: "+xdsIti57Endpoint);

		XdsClientFactory xdsClientFactory = generateXdsRegistryClientFactory("urn:ihe:iti:xds-b:2010", xdsIti57Wsdl, xdsIti57Endpoint, Iti57PortType.class);
		Iti57PortType client = (Iti57PortType) xdsClientFactory.getClient();

		initProxy(client);
		setSoapActionHeader((BindingProvider) client, "urn:ihe:iti:2010:UpdateDocumentSet");

		return client;
	}

	private XdsClientFactory generateXdsRegistryClientFactory(String wsdl, String url, Class<?> clazz){
		return generateXdsRegistryClientFactory("urn:ihe:iti:xds-b:2007", wsdl, url, clazz);
	}

	private XdsClientFactory generateXdsRegistryClientFactory(String namespace, String wsdl, String url, Class<?> clazz){
		final WsTransactionConfiguration WS_CONFIG = new WsTransactionConfiguration(
				new QName(namespace, "DocumentRegistry_Service",
						"ihe"), clazz, new QName(
								namespace,
								"DocumentRegistry_Binding_Soap12", "ihe"), false,
				wsdl, true, false, false, false);

		return new XdsClientFactory(WS_CONFIG, url, null, null,null);
	}

	private XdsClientFactory generateXdsRepositoryClientFactory(String wsdl, String url, Class<?> clazz){
		final WsTransactionConfiguration WS_CONFIG = new WsTransactionConfiguration(
				new QName("urn:ihe:iti:xds-b:2007", "DocumentRepository_Service",
						"ihe"), clazz, new QName(
								"urn:ihe:iti:xds-b:2007",
								"DocumentRepository_Binding_Soap12", "ihe"), true,
				wsdl, true, false, false, false);
		

		XdsClientFactory xcf = new XdsClientFactory(WS_CONFIG, url, null, null,null);
		return xcf;
	}
	
	private void initProxy(Object o) {
		
		Client proxy = ClientProxy.getClient(o);		
		if (LOGGER.isDebugEnabled()) {
			proxy.getOutInterceptors().add(new LoggingOutInterceptor());
			proxy.getInInterceptors().add(new LoggingInInterceptor());
		}	
		HTTPConduit conduit = (HTTPConduit)proxy.getConduit();
		TLSClientParameters tcp = new TLSClientParameters();
		tcp.setTrustManagers(trustAllCerts);
		conduit.setTlsClientParameters(tcp);
	}
	private void setSoapActionHeader(BindingProvider bindingProvider, String soapActionValue) {
		Map<String, List<String>> headers = (Map<String, List<String>>) bindingProvider.getRequestContext().get(MessageContext.HTTP_REQUEST_HEADERS);
		if (headers == null) {
			headers = new HashMap<>();
		}
		headers.put("SOAPAction", Collections.singletonList(soapActionValue));
		bindingProvider.getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, headers);
	}

	@Bean
	public XdsRequestBuilderService xdsRequestBuilderService() {
		XdsRequestBuilderService xdsRequestBuilderService = new XdsRequestBuilderService();
		return xdsRequestBuilderService;
	}

	@Bean
	public OrganisationIdAuthority organisationIdAuthority() {
		return new OrganisationIdAuthority(NSI.SOR_OID);
	}

	@Bean
	public XdsRequestService xdsRequestService() {
		XdsRequestService xdsRequestService = new XdsRequestService();
		return xdsRequestService;
	}
	
	@Bean
	public CdaMetaDataFactory cdaMetaDataFactory() {
		return new CdaMetaDataFactory();
	}
	
	@Bean
	public DocumentProcessor documentProcessor() {
		return new DocumentProcessor();
	}
	
}
