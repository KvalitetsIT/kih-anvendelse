package dk.kvalitetsit.cda.services;

import java.util.LinkedList;

import dk.kvalitetsit.cda.dto.CdaMetadata;
import dk.kvalitetsit.cda.dto.Code;
import dk.kvalitetsit.cda.dto.DocumentMetadata;
import dk.kvalitetsit.cda.dto.Person;
import dk.s4.hl7.cda.convert.CDAMetadataXmlCodec;
import dk.s4.hl7.cda.model.CodedValue;
import dk.s4.hl7.cda.model.cdametadata.CDAMetadata;

public class CdaMetaDataFactory {
	
	public DocumentMetadata getMetadata(CdaMetadata cdaMetadata, String document) {
		
		DocumentMetadata documentMetadata = new DocumentMetadata();
		if(cdaMetadata != null){
			documentMetadata.setAvailabilityStatus(cdaMetadata.getAvailabilityStatus());
			documentMetadata.setClassCode(cdaMetadata.getClassCode());
			documentMetadata.setFormatCode(cdaMetadata.getFormatCode());
			documentMetadata.setHealthcareFacilityTypeCode(cdaMetadata.getHealthcareFacilityTypeCode());
			documentMetadata.setObjectType(cdaMetadata.getObjectType());
			documentMetadata.setPracticeSettingCode(cdaMetadata.getPracticeSettingCode());
			documentMetadata.setSubmissionTime(cdaMetadata.getSubmissionTime());
		}
		
		getMetadataFromDocument(documentMetadata, document);
		return documentMetadata;
	}
	
	private void getMetadataFromDocument(DocumentMetadata documentMetadata, String document) {

		CDAMetadataXmlCodec codec = new CDAMetadataXmlCodec();
		CDAMetadata cdaMetadataDecoded = codec.decode(document);
		
		//author.authorInstitution - organization
		if (cdaMetadataDecoded.getAuthor() != null && cdaMetadataDecoded.getAuthorInstitution() != null && 
				cdaMetadataDecoded.getAuthorInstitution().getCode() != null && cdaMetadataDecoded.getAuthorInstitution().getCodeSystem() != null) {
			Code autherOrganizationCode = new Code(cdaMetadataDecoded.getAuthorInstitution().getDisplayName(), cdaMetadataDecoded.getAuthorInstitution().getCode(), cdaMetadataDecoded.getAuthorInstitution().getCodeSystem());

			documentMetadata.setOrganisation(autherOrganizationCode);
		}
		
		//auther.authorperson
		if (cdaMetadataDecoded.getAuthor() != null && cdaMetadataDecoded.getAuthorperson() != null) {
			Person authorPerson = new Person();
			if (cdaMetadataDecoded.getAuthorperson().getFamilyName() != null) {
				authorPerson.setFamilyName(cdaMetadataDecoded.getAuthorperson().getFamilyName());
			}
			if (cdaMetadataDecoded.getAuthorperson().getGivenNames() != null && cdaMetadataDecoded.getAuthorperson().getGivenNames().length > 0) {
				authorPerson.setGivenName(cdaMetadataDecoded.getAuthorperson().getGivenNames()[0]);
				if (cdaMetadataDecoded.getAuthorperson().getGivenNames().length > 1) {
					authorPerson.setSecondAndFurtherGivenNames(cdaMetadataDecoded.getAuthorperson().getGivenNames()[1]);
					for (int i = 2; i< cdaMetadataDecoded.getAuthorperson().getGivenNames().length;i++) { 
						authorPerson.setSecondAndFurtherGivenNames(authorPerson.getSecondAndFurtherGivenNames() + "&" + cdaMetadataDecoded.getAuthorperson().getGivenNames()[i]);
					}
				}
			}
			documentMetadata.setAuthorPerson(authorPerson);
		}
		
		//confidentialityCode
		if (cdaMetadataDecoded.getConfidentialityCodeCodedValue() != null && cdaMetadataDecoded.getConfidentialityCodeCodedValue().getCode() != null && cdaMetadataDecoded.getConfidentialityCodeCodedValue().getCodeSystem() != null) {
			Code confidentialityCode = new Code(cdaMetadataDecoded.getConfidentialityCodeCodedValue().getDisplayName(), cdaMetadataDecoded.getConfidentialityCodeCodedValue().getCode(), cdaMetadataDecoded.getConfidentialityCodeCodedValue().getCodeSystem());
			documentMetadata.setConfidentialityCode(confidentialityCode);
	
		}
		
		//contentTypeCode - not used
		
		//creationTime
		if (cdaMetadataDecoded.getCreationTime() != null) {
			documentMetadata.setReportTime(cdaMetadataDecoded.getCreationTime());
		}
		
		//eventCodedList
		for (CodedValue event : cdaMetadataDecoded.getEventCodeList()) {
			if(documentMetadata.getEventCodes() == null){
				documentMetadata.setEventCodes(new LinkedList<Code>());
			}
			Code eventCode = new Code(event.getDisplayName(), event.getCode(), event.getCodeSystem());
			documentMetadata.getEventCodes().add(eventCode);
		}

		//LanguageCode
		documentMetadata.setLanguageCode(cdaMetadataDecoded.getLanguageCode());
		
		//legalAuthenticator
		if (cdaMetadataDecoded.getLegalAuthenticator() != null && cdaMetadataDecoded.getLegalAuthenticator().getPersonIdentity() != null) {
			Person legalAuthenticator = new Person();	
			if (cdaMetadataDecoded.getLegalAuthenticator().getPersonIdentity().getFamilyName() != null) {
				legalAuthenticator.setFamilyName(cdaMetadataDecoded.getLegalAuthenticator().getPersonIdentity().getFamilyName());
			}
			if (cdaMetadataDecoded.getLegalAuthenticator().getPersonIdentity().getGivenNames() != null && cdaMetadataDecoded.getLegalAuthenticator().getPersonIdentity().getGivenNames().length > 0) {
				legalAuthenticator.setGivenName(cdaMetadataDecoded.getLegalAuthenticator().getPersonIdentity().getGivenNames()[0]);
				if (cdaMetadataDecoded.getLegalAuthenticator().getPersonIdentity().getGivenNames().length > 1) {
					legalAuthenticator.setSecondAndFurtherGivenNames(cdaMetadataDecoded.getLegalAuthenticator().getPersonIdentity().getGivenNames()[1]);
					for (int i = 2; i< cdaMetadataDecoded.getLegalAuthenticator().getPersonIdentity().getGivenNames().length;i++) {
						legalAuthenticator.setSecondAndFurtherGivenNames(legalAuthenticator.getSecondAndFurtherGivenNames() + "&" + cdaMetadataDecoded.getLegalAuthenticator().getPersonIdentity().getGivenNames()[i]);
					}		
				}
			}
			documentMetadata.setLegalAuthenticator(legalAuthenticator);
		}
		
		//patientId
		if (cdaMetadataDecoded.getPatient() != null && cdaMetadataDecoded.getPatientId() != null && cdaMetadataDecoded.getPatientId().getCode() != null && cdaMetadataDecoded.getPatientId().getCodeSystem() != null) {
			Code patientIdCode = new Code("", cdaMetadataDecoded.getPatientId().getCode(), cdaMetadataDecoded.getPatientId().getCodeSystem());
			documentMetadata.setPatientId(patientIdCode);
		}
		
		//serviceStartTime
		if (cdaMetadataDecoded.getServiceStartTime() != null) {
			documentMetadata.setServiceStartTime(cdaMetadataDecoded.getServiceStartTime());
		}
		
		//serviceStopTime
		if (cdaMetadataDecoded.getServiceStopTime() != null) {
			documentMetadata.setServiceStopTime(cdaMetadataDecoded.getServiceStopTime());
		}
		
		//sourcePatientId
		if (cdaMetadataDecoded.getPatient() != null && cdaMetadataDecoded.getSourcePatientId() != null && cdaMetadataDecoded.getSourcePatientId().getCode() != null && cdaMetadataDecoded.getSourcePatientId().getCodeSystem() != null) {
			Code sourcePatientIdCode = new Code("", cdaMetadataDecoded.getSourcePatientId().getCode(), cdaMetadataDecoded.getSourcePatientId().getCodeSystem());
			documentMetadata.setSourcePatientId(sourcePatientIdCode);
		}

		//sourcePatientInfo
		if (cdaMetadataDecoded.getPatient() != null) {
			Person sourcePatientInfoPerson = new Person();
			if (cdaMetadataDecoded.getPatient().getFamilyName() != null) {
				sourcePatientInfoPerson.setFamilyName(cdaMetadataDecoded.getPatient().getFamilyName());
			}
			if (cdaMetadataDecoded.getPatient().getGivenNames() != null && cdaMetadataDecoded.getPatient().getGivenNames().length > 0) {
				sourcePatientInfoPerson.setGivenName(cdaMetadataDecoded.getPatient().getGivenNames()[0]);
				if (cdaMetadataDecoded.getPatient().getGivenNames().length > 1) {
					sourcePatientInfoPerson.setSecondAndFurtherGivenNames(cdaMetadataDecoded.getPatient().getGivenNames()[1]);
					for (int i = 2; i< cdaMetadataDecoded.getPatient().getGivenNames().length;i++) { 
						sourcePatientInfoPerson.setSecondAndFurtherGivenNames(sourcePatientInfoPerson.getSecondAndFurtherGivenNames() + "&" + cdaMetadataDecoded.getPatient().getGivenNames()[i]);
					}
				}
			}
			documentMetadata.setSourcePatientInfoPerson(sourcePatientInfoPerson);
			if (cdaMetadataDecoded.getPatient().getBirthTime() != null) {
				documentMetadata.setSourcePatientInfoDateOfBirth(cdaMetadataDecoded.getPatient().getBirthTime());
			}
			if (cdaMetadataDecoded.getPatient().getGender() != null) {
				documentMetadata.setSourcePatientInfoGender(cdaMetadataDecoded.getPatient().getGender().name().substring(0,1));
			}
		}
		
		//title
		if (cdaMetadataDecoded.getTitle() != null) {
			documentMetadata.setTitle(cdaMetadataDecoded.getTitle());
		}
		
		//typeCode
		if ((cdaMetadataDecoded.getCodeCodedValue() != null) && (cdaMetadataDecoded.getCodeCodedValue().getCode() != null) && (cdaMetadataDecoded.getCodeCodedValue().getCodeSystem() != null) && (cdaMetadataDecoded.getCodeCodedValue().getDisplayName() != null)) {
			Code typeCode = new Code(cdaMetadataDecoded.getCodeCodedValue().getDisplayName(), cdaMetadataDecoded.getCodeCodedValue().getCode(), cdaMetadataDecoded.getCodeCodedValue().getCodeSystem());
			documentMetadata.setTypeCode(typeCode);
		}
		
		//uniqeId
		if (cdaMetadataDecoded.getId() != null && cdaMetadataDecoded.getId().getExtension() != null && cdaMetadataDecoded.getId().getRoot() != null) {
			documentMetadata.setUniqueId(cdaMetadataDecoded.getId().getRoot() + "^" + cdaMetadataDecoded.getId().getExtension());
		}
		
	}


}
