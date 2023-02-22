package dk.kvalitetsit.cda.services;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.openehealth.ipf.commons.ihe.xds.core.metadata.DocumentEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import dk.kvalitetsit.cda.configuration.PatientContext;
import dk.kvalitetsit.cda.document.DocumentHelper;
import dk.kvalitetsit.cda.dto.DocumentMetadata;
import dk.s4.hl7.cda.model.util.DateUtil;

public class DocumentProcessor {
	
	@Autowired
	PatientContext userContext;
	
	@Autowired
	XdsRequestService xdsRequestService;
	
	@Autowired
	CdaMetaDataFactory cdaMetaDataFactory;
	
	@Value("${xds.homecommunityid}")
	String homeCommunityId;

	
	public void runCDADocument(DocumentHelper documentHelper) throws Exception {
		
		
		// SEARCH --------------------------------------------------------------------------------------- //
		
		// Search documents for patient
		List<DocumentEntry> currentDocuments = xdsRequestService.getDocumentsForPatient(userContext.getPatientId(), documentHelper.getDocumentTypeCode());
		System.out.println("The patient with id="+userContext.getPatientId()+" has "+currentDocuments.size()+" registered in the XDS registry.");

		
		// CREATE -------------------------------------------------------------------------------------- //
		
		// Get new id for the document
//		String externalIdForNewDocument = generateUUIDasOID(); //Some backends requires this format as document id 
		String externalIdForNewDocument = generateUUID();
		
		// Set from/to time for the document		
	    Date from = DateUtil.makeDanishDateTimeWithTimeZone(2019, 0, 6, 8, 2, 0);
	    Date to = DateUtil.makeDanishDateTimeWithTimeZone(2019, 0, 10, 8, 15, 0);
	    
	
		// Create the XML document using the builder/parser
		String xmlDocument = documentHelper.createDocumentAsXML(externalIdForNewDocument, from, to);

		// Create metadata using builder/parser  and based on document standard metadata
		DocumentMetadata documentMetadata = cdaMetaDataFactory.getMetadata(documentHelper.createCdaMetadata(homeCommunityId), xmlDocument);
		
		// Register the documement
		String documentIdNew = xdsRequestService.createAndRegisterDocument(xmlDocument , documentMetadata);
		System.out.println("We registered a new document with documentId="+documentIdNew);

		// Search document for patient (we assume there is one more now)
		List<DocumentEntry> currentDocumentsAfterNew = xdsRequestService.getDocumentsForPatient(userContext.getPatientId(), documentHelper.getDocumentTypeCode());
		boolean documentIsInList = isDocumentIdInList(documentIdNew, currentDocumentsAfterNew);
		System.out.println("The patient with id="+userContext.getPatientId()+" now has "+currentDocumentsAfterNew.size()+" registered in the XDS registry after create. DocumentId:"+documentIdNew+" "+(documentIsInList ? "could": "COULDN'T BUT SHOULD")+" be found.");
		
		
		// READ ---------------------------------------------------------------------------------------- //
		
		// get the document
		String homeCommunityIdRead = null; 
		String repositoryIdRead = null;
		if (documentIsInList) {
			DocumentEntry documentEntry = getDocumentInList(documentIdNew, currentDocumentsAfterNew);
			homeCommunityIdRead = documentEntry.getHomeCommunityId();
			repositoryIdRead = documentEntry.getRepositoryUniqueId();
		}
		String document = xdsRequestService.fetchDocument(documentIdNew, homeCommunityIdRead, repositoryIdRead);
		
		
		// read the xml into an cdaDocument format with the builder/parser and fetch the effectiveTime
		Date effectiveTime  = documentHelper.getDocumentEffectiveTimeFromXML(document);
		System.out.println("The document has the following Effective Date: " + effectiveTime.toString());
		
		
		// REPLACE -------------------------------------------------------------------------------------- //

		// Get new id for the new document, ids must not be reused
//		String externalIdForUpdatedDocument = generateUUIDasOID(); //Some backends requires this format as document id
		String externalIdForUpdatedDocument = generateUUID();
		
		// Updates to the document - new time
	    Date updatedFrom = DateUtil.makeDanishDateTimeWithTimeZone(2019, 1, 6, 8, 2, 0);
	    Date updatedTo = DateUtil.makeDanishDateTimeWithTimeZone(2019, 1, 10, 8, 15, 0);


		// Create the XML document using the builder/parser
		xmlDocument = documentHelper.createDocumentAsXML(externalIdForUpdatedDocument, updatedFrom, updatedTo);

		// Create metadata using builder/parser and based on document standard metadata
		DocumentMetadata updatedDocumentMetadata = cdaMetaDataFactory.getMetadata(documentHelper.createCdaMetadata(homeCommunityId), xmlDocument);
		
		// Get the document entryUUid for the one to be updated
		DocumentEntry toBeUpdated = xdsRequestService.getDocumentEntry(documentIdNew);		
		
		// Update the documement by registering the new document as a replacement
		String documentIdUpdated = xdsRequestService.createAndRegisterDocumentAsReplacement(xmlDocument, updatedDocumentMetadata, toBeUpdated.getEntryUuid());
		System.out.println("We registered a replacement document with documentId="+documentIdUpdated);
		
		// Search document for patient (we assume there is the same number as before)
		List<DocumentEntry> currentDocumentssAfterUpdatedDocuments = xdsRequestService.getDocumentsForPatient(userContext.getPatientId(), documentHelper.getDocumentTypeCode());
		boolean couldFindOld = isDocumentIdInList(documentIdNew, currentDocumentssAfterUpdatedDocuments);
		boolean couldFindNew = isDocumentIdInList(documentIdUpdated, currentDocumentssAfterUpdatedDocuments);
		System.out.println("The patient with id="+userContext.getPatientId()+" now has "+currentDocumentssAfterUpdatedDocuments.size()+
				" registered in the XDS registry after update. The old DocumentId: "+documentIdNew+" "+(couldFindOld ? "COULD BUT SHOULDN'T" : "could correctly not")+" be found in search." + 
				" The new DocumentId:"+documentIdUpdated+" "+(couldFindNew ? "could" : "COULDN'T BUT SHOULD")+" be found.");

		
		// DEPRECATE ----------------------------------------------------------------------------------- //

		// Get the document entryUUid for the one to be deprecated
		DocumentEntry toBeDeprecated = xdsRequestService.getDocumentEntry(documentIdUpdated);		

		// Deprecate the document
		xdsRequestService.deprecateDocument(toBeDeprecated);

		// Search document for patient (we assume there is one less than before the deprecate)
		List<DocumentEntry> currentDocumentsAfterDeprecation = xdsRequestService.getDocumentsForPatient(userContext.getPatientId(), documentHelper.getDocumentTypeCode());
		boolean deprecatedDocumentIsInList = isDocumentIdInList(documentIdUpdated, currentDocumentsAfterDeprecation);
		System.out.println("The patient with id="+userContext.getPatientId()+" now has "+currentDocumentsAfterDeprecation.size()+
				" registered in the XDS registry after deprecate. DocumentId: "+documentIdUpdated+" "+(deprecatedDocumentIsInList ? "COULD BUT SHOULDN'T": "could correctly not")+" be found.");
		
	}
	
	private String generateUUID() {
		return java.util.UUID.randomUUID().toString();
	}
	
	private String generateUUIDasOID() {
		java.util.UUID uuid = java.util.UUID.randomUUID();
		return Math.abs(uuid.getLeastSignificantBits()) + "." + Math.abs(uuid.getMostSignificantBits())+"."+Calendar.getInstance().getTimeInMillis();
	}
	
	private boolean isDocumentIdInList(String id, List<DocumentEntry> documentEntries) {
		for (DocumentEntry documentEntry : documentEntries) {
			if (documentEntry.getUniqueId().equals(id)) {
				return true;
			}
		}
		return false;
	}
	
	private DocumentEntry getDocumentInList(String id, List<DocumentEntry> documentEntries) {
		for (DocumentEntry documentEntry : documentEntries) {
			if (documentEntry.getUniqueId().equals(id)) {
				return documentEntry;
			}
		}
		return null;
	}


}
