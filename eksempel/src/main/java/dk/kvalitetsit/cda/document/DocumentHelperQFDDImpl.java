package dk.kvalitetsit.cda.document;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;

import org.openehealth.ipf.commons.ihe.xds.core.metadata.AvailabilityStatus;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.Code;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.DocumentEntryType;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.LocalizedString;

import dk.kvalitetsit.cda.dto.CdaMetadata;
import dk.kvalitetsit.cda.exceptions.ParserException;
import dk.s4.hl7.cda.codes.Loinc;
import dk.s4.hl7.cda.convert.QFDDXmlCodec;
import dk.s4.hl7.cda.model.qfdd.QFDDDocument;

public class DocumentHelperQFDDImpl implements DocumentHelper {
	
	private DocumentFactoryQFDDImpl documentFactoryQfddImpl;
	private static QFDDXmlCodec codec = new QFDDXmlCodec();
	private static Code QFDD_CODE = new Code(Loinc.QFD_CODE, new LocalizedString(Loinc.QFD_DISPLAYNAME), Loinc.OID);
	
	public DocumentHelperQFDDImpl() {
		documentFactoryQfddImpl = new DocumentFactoryQFDDImpl();
	}
	
	public String createDocumentAsXML(String externalIdForNewDocument, Date from, Date to) throws ParserException {
		
		if (documentFactoryQfddImpl == null) {
			documentFactoryQfddImpl = new DocumentFactoryQFDDImpl();
		}
		
		QFDDDocument qfddDocument;
		try {
			qfddDocument = documentFactoryQfddImpl.defineAsCDA(externalIdForNewDocument, from, to);
		} catch (IOException | URISyntaxException e) {
			throw new ParserException("Error during cration of CDA document as XML", e);
		}
		String xmlDocument = codec.encode(qfddDocument);
		return xmlDocument;
	}
	
	public Date getDocumentEffectiveTimeFromXML(String document) {
		QFDDDocument qfddDocument = codec.decode(document);	
		return qfddDocument.getEffectiveTime();
	}
	
	public Code getDocumentTypeCode() {
		return QFDD_CODE;
	}	
	
	public CdaMetadata createCdaMetadata() {
		CdaMetadata cdaMetaData = new CdaMetadata();
		cdaMetaData.setAvailabilityStatus(AvailabilityStatus.APPROVED);
		cdaMetaData.setObjectType(DocumentEntryType.STABLE);
		cdaMetaData.setClassCode(new dk.kvalitetsit.cda.dto.Code("Klinisk rapport","001", "1.2.208.184.100.9"));
		cdaMetaData.setFormatCode(new dk.kvalitetsit.cda.dto.Code("QFDD DK schema", "urn:ad:dk:medcom:qfdd:full", "1.2.208.184.100.10"));//TODO: er denne korrekt?
		cdaMetaData.setHealthcareFacilityTypeCode(new dk.kvalitetsit.cda.dto.Code("sundhedscenter","264361005","2.16.840.1.113883.6.96"));
		cdaMetaData.setPracticeSettingCode(new dk.kvalitetsit.cda.dto.Code("almen medicin", "408443003", "2.16.840.1.113883.6.96"));
		cdaMetaData.setSubmissionTime(new Date());
		return cdaMetaData;
	}
	
}
