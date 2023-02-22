package dk.kvalitetsit.cda.document;

import java.util.Date;

import org.openehealth.ipf.commons.ihe.xds.core.metadata.Code;

import dk.kvalitetsit.cda.dto.CdaMetadata;
import dk.kvalitetsit.cda.exceptions.ParserException;

public interface DocumentHelper {
	
	// The document specific details of format, codec etc is wrapped inside this interface. 
	// This is to allow for all documents independent of cda header version etc. to be handled
	
	public String createDocumentAsXML(String externalIdForNewDocument, Date from, Date to) throws ParserException;
	public Date getDocumentEffectiveTimeFromXML(String document);
	public Code getDocumentTypeCode();
	public CdaMetadata createCdaMetadata(String homeCommunityId);
}
