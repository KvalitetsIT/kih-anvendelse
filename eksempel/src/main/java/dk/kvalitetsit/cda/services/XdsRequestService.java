package dk.kvalitetsit.cda.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.openehealth.ipf.commons.ihe.xds.core.ebxml.EbXMLFactory;
import org.openehealth.ipf.commons.ihe.xds.core.ebxml.ebxml30.*;
import org.openehealth.ipf.commons.ihe.xds.core.ebxml.ebxml30.RetrieveDocumentSetResponseType.DocumentResponse;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.Code;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.DocumentEntry;
import org.openehealth.ipf.commons.ihe.xds.core.requests.query.QueryReturnType;
import org.openehealth.ipf.commons.ihe.xds.core.responses.*;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.lcm.SubmitObjectsRequest;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.query.AdhocQueryRequest;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.query.AdhocQueryResponse;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rs.RegistryError;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rs.RegistryResponseType;
import org.openehealth.ipf.commons.ihe.xds.core.transform.responses.QueryResponseTransformer;
import org.openehealth.ipf.commons.ihe.xds.core.transform.responses.ResponseTransformer;
import org.openehealth.ipf.commons.ihe.xds.iti18.Iti18PortType;
import org.openehealth.ipf.commons.ihe.xds.iti41.Iti41PortType;
import org.openehealth.ipf.commons.ihe.xds.iti43.Iti43PortType;
import org.openehealth.ipf.commons.ihe.xds.iti57.Iti57PortType;
import org.springframework.beans.factory.annotation.Autowired;

import dk.kvalitetsit.cda.dto.DocumentMetadata;
import dk.kvalitetsit.cda.exceptions.XdsException;

public class XdsRequestService {

	private static final EbXMLFactory ebXMLFactory = new EbXMLFactory30();

	@Autowired
	XdsRequestBuilderService xdsRequestBuilderService;

	@Autowired
	Iti57PortType iti57PortType;

	@Autowired
	Iti43PortType iti43PortType;

	@Autowired
	Iti18PortType iti18PortType;

	@Autowired
	Iti41PortType iti41PortType;

	public List<DocumentEntry> getDocumentsForPatient(String citizenId) throws XdsException {
		return getDocumentsForPatient(citizenId, null, null, null);
	}
	
	public List<DocumentEntry> getDocumentsForPatient(String citizenId, Code typeCode) throws XdsException {
		List<Code> typeCodes = new ArrayList<Code>();
		typeCodes.add(typeCode);
		return getDocumentsForPatient(citizenId, typeCodes, null, null);
	}
	
	public List<DocumentEntry> getDocumentsForPatient(String citizenId, List<Code> typeCodes, Date start, Date end) {
		AdhocQueryRequest adhocQueryRequest = xdsRequestBuilderService.buildAdhocQueryRequest(citizenId, typeCodes, start, end);
		AdhocQueryResponse adhocQueryResponse = iti18PortType.documentRegistryRegistryStoredQuery(adhocQueryRequest);
		if (adhocQueryResponse.getRegistryErrorList() != null && !adhocQueryResponse.getRegistryErrorList().getRegistryError().isEmpty()) {
			//in case of any errors log them, the search might has gone okay any way. DDS might have some issues with a specific non relevant backend
			for (RegistryError error : adhocQueryResponse.getRegistryErrorList().getRegistryError()) {
				System.out.println("Error received from registry [errorCode:"+error.getErrorCode()+", errorValue:"+error.getValue()+ ", codeContext:" + error.getCodeContext() +"]");
			}
		} 
		QueryResponseTransformer queryResponseTransformer = new QueryResponseTransformer(getEbXmlFactory());
		EbXMLQueryResponse30 ebXmlresponse = new EbXMLQueryResponse30(adhocQueryResponse);
		QueryResponse queryResponse = queryResponseTransformer.fromEbXML(ebXmlresponse);
		List<DocumentEntry> docEntries = queryResponse.getDocumentEntries();
		return docEntries;
	}

	public DocumentEntry getDocumentEntry(String documentId) throws XdsException {

		AdhocQueryRequest adhocQueryRequest = xdsRequestBuilderService.buildAdhocQueryRequest(documentId, QueryReturnType.LEAF_CLASS);
		AdhocQueryResponse adhocQueryResponse = iti18PortType.documentRegistryRegistryStoredQuery(adhocQueryRequest);

		if (!Status.SUCCESS.getOpcode30().equals(adhocQueryResponse.getStatus()) && (!Status.PARTIAL_SUCCESS.getOpcode30().equals(adhocQueryResponse.getStatus())) && adhocQueryResponse.getRegistryErrorList() != null && !adhocQueryResponse.getRegistryErrorList().getRegistryError().isEmpty()) {
			throw new XdsException(adhocQueryResponse.getRegistryErrorList());
		} else {
			QueryResponseTransformer queryResponseTransformer = new QueryResponseTransformer(getEbXmlFactory());
			EbXMLQueryResponse30 ebXmlresponse = new EbXMLQueryResponse30(adhocQueryResponse);
			QueryResponse queryResponse = queryResponseTransformer.fromEbXML(ebXmlresponse);
			List<DocumentEntry> docEntries = queryResponse.getDocumentEntries();
			DocumentEntry documentEntry = docEntries.get(0);
			return documentEntry;
		}
	}

	public String fetchDocument(String documentId) throws IOException, XdsException {
		return fetchDocument(documentId, null, null);
	}	

	public Map<String, String> fetchDocuments(List<String> documentIds, String homeCommunityId, String repositoryId) throws XdsException {

		Map<String, String> documents = new HashMap<>();
		
		RetrieveDocumentSetRequestType rdsrt = null;
		if (repositoryId != null && homeCommunityId != null) {
			rdsrt = xdsRequestBuilderService.buildRetrieveDocumentSetRequestType(documentIds, homeCommunityId, repositoryId);
		} else {
			if (repositoryId != null) {
				rdsrt = xdsRequestBuilderService.buildRetrieveDocumentSetRequestType(documentIds, null, repositoryId);
			} else {
			rdsrt = xdsRequestBuilderService.buildRetrieveDocumentSetRequestType(documentIds);
			}
		}

		RetrieveDocumentSetResponseType repositoryResponse= iti43PortType.documentRepositoryRetrieveDocumentSet(rdsrt);
		if (repositoryResponse.getRegistryResponse().getRegistryErrorList() == null || repositoryResponse.getRegistryResponse().getRegistryErrorList().getRegistryError() == null || repositoryResponse.getRegistryResponse().getRegistryErrorList().getRegistryError().isEmpty()) {
			// if no documents an error is produced, get(0) should work.
			
			Iterator<DocumentResponse> documentIterator = repositoryResponse.getDocumentResponse().iterator();
			while (documentIterator.hasNext()) {
				DocumentResponse documentResponse = documentIterator.next();
				try {
					String documentString = new BufferedReader(new InputStreamReader(documentResponse.getDocument().getInputStream())).lines().collect(Collectors.joining());
					documents.put(documentResponse.getDocumentUniqueId(), documentString);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

		} else {
			XdsException e = new XdsException();
			for (RegistryError registryError :repositoryResponse.getRegistryResponse().getRegistryErrorList().getRegistryError()) {
				e.addError(registryError.getCodeContext());
			}
			throw e;
		}
		return documents;
	}
	
	public String fetchDocument(String documentId, String homeCommunityId, String repositoryId) throws IOException, XdsException {
		List<String> documentIds = new LinkedList<String>();
		documentIds.add(documentId);
		Map<String, String> documentResult = fetchDocuments(documentIds, homeCommunityId, repositoryId);
		return documentResult.get(documentId);
	}

	public String createAndRegisterDocument(String document, DocumentMetadata documentMetadata) throws XdsException {
		return createAndRegisterDocument(document, documentMetadata, null);
	}

	public String createAndRegisterDocument(String document, DocumentMetadata documentMetadata, String externalIdForDocumentToReplace) throws XdsException {
		ProvideAndRegisterDocumentSetRequestType provideAndRegisterDocumentSetRequest = xdsRequestBuilderService.buildProvideAndRegisterDocumentSetRequest(document, documentMetadata, null);
		RegistryResponseType registryResponse = iti41PortType.documentRepositoryProvideAndRegisterDocumentSetB(provideAndRegisterDocumentSetRequest);

		ResponseTransformer responseTransformer = new ResponseTransformer(getEbXmlFactory());
		EbXMLRegistryResponse30 ebxml = new EbXMLRegistryResponse30(registryResponse);
		Response response = responseTransformer.fromEbXML(ebxml);

		// log warnings
		if (response.getErrors() != null && response.getErrors().size() > 0) {
			for (ErrorInfo registryError : response.getErrors()) {
				if (!Severity.ERROR.equals(registryError.getSeverity())) {
					System.out.println("Warning: " + registryError.getCodeContext());
				}
			}
		}

		if (response.getStatus() == Status.SUCCESS) {
			return documentMetadata.getUniqueId();
		} else {
			XdsException e = new XdsException();
			if (response.getErrors() != null) {
				for (ErrorInfo registryError : response.getErrors()) {
					if (Severity.ERROR.equals(registryError.getSeverity())) {
						e.addError(registryError.getCodeContext());
					}
				}
			}

			throw e;
		}
	}

	protected EbXMLFactory getEbXmlFactory() {
		return ebXMLFactory;
	}


	public void deprecateDocument(DocumentEntry toBeDeprecated) throws XdsException {
		SubmitObjectsRequest body = xdsRequestBuilderService.buildDeprecateSubmitObjectsRequest(toBeDeprecated);		
		RegistryResponseType registryResponse = iti57PortType.documentRegistryUpdateDocumentSet(body);

		if (registryResponse.getRegistryErrorList() == null || registryResponse.getRegistryErrorList().getRegistryError() == null || registryResponse.getRegistryErrorList().getRegistryError().isEmpty()) {
			//OK !
		} else {
			XdsException e = new XdsException();
			for (RegistryError registryError :registryResponse.getRegistryErrorList().getRegistryError()) {
				e.addError(registryError.getCodeContext());
			}
			throw e;
		}
	}

	public void addOutInterceptors(AbstractPhaseInterceptor<Message> interceptor) {		
		addOutInterceptor(iti18PortType, interceptor);
		addOutInterceptor(iti41PortType, interceptor);		
		addOutInterceptor(iti43PortType, interceptor);
		addOutInterceptor(iti57PortType, interceptor);
	}

	private void addOutInterceptor(Object o, AbstractPhaseInterceptor<Message> interceptor) {
		Client proxy = ClientProxy.getClient(o);
		proxy.getOutInterceptors().add(interceptor);
	}

	public void addInInterceptors(AbstractPhaseInterceptor<Message> interceptor) {		
		addInInterceptor(iti18PortType, interceptor);
		addInInterceptor(iti41PortType, interceptor);		
		addInInterceptor(iti43PortType, interceptor);
		addInInterceptor(iti57PortType, interceptor);
	}

	private void addInInterceptor(Object o, AbstractPhaseInterceptor<Message> interceptor) {
		Client proxy = ClientProxy.getClient(o);
		proxy.getInInterceptors().add(interceptor);
	}
}
