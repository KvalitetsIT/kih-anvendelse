package dk.kvalitetsit.cda.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import dk.kvalitetsit.cda.document.DocumentFactoryPHMRImpl;
import dk.kvalitetsit.cda.services.CdaMetaDataFactory;
import dk.s4.hl7.cda.convert.PHMRXmlCodec;
import dk.s4.hl7.cda.model.phmr.PHMRDocument;
import dk.s4.hl7.cda.model.util.DateUtil;

public class BuilderParserPHMRTest {
	
	CdaMetaDataFactory cdaMetaDataFactory;

	DocumentFactoryPHMRImpl phmrDocumentFactory;
	
	private static PHMRXmlCodec codec = new PHMRXmlCodec();

	@Before
	public void init() {
		cdaMetaDataFactory = new CdaMetaDataFactory();
		phmrDocumentFactory = new DocumentFactoryPHMRImpl();
	}
	

	@Test
	public void testCreatePHMR() throws ParseException {

		// Given 
		String externalIdForDocument = generateUUID();
		Date from = DateUtil.makeDanishDateTime(2019, 0, 6, 8, 2, 0);
	    Date to = DateUtil.makeDanishDateTime(2019, 0, 10, 8, 15, 0);
		
		// When
		PHMRDocument phmrDocument = phmrDocumentFactory.defineAsCDA(externalIdForDocument, from, to);
		
		// Then
		assertNotNull(phmrDocument);
		assertNotNull(phmrDocument.getId().getRoot());
		assertNotNull(phmrDocument.getId().getExtension());
		assertNotNull(phmrDocument.getVitalSignsText());
		assertEquals("Vital Signs", phmrDocument.getVitalSignsText());
		assertNotNull(phmrDocument.getVitalSigns());
		assertEquals(3, phmrDocument.getVitalSigns().size());
		assertNotNull(phmrDocument.getVitalSigns().get(0).getCode());
		assertEquals("NPU03011", phmrDocument.getVitalSigns().get(0).getCode());
				
	}
	
	@Test
	public void testBuildXml() throws ParseException {

		// Given 
		String externalIdForDocument = generateUUID();
		Date from = DateUtil.makeDanishDateTime(2019, 0, 6, 8, 2, 0);
	    Date to = DateUtil.makeDanishDateTime(2019, 0, 10, 8, 15, 0);
		PHMRDocument phmrDocument = phmrDocumentFactory.defineAsCDA(externalIdForDocument, from, to);
		
		// When
		String xmlDocument = codec.encode(phmrDocument);
		
		// Then		
		assertNotNull(xmlDocument);
		assertTrue(xmlDocument.length() > 200);
		assertTrue(xmlDocument.contains("NPU03011"));
		
	}
	
	@Test
	public void testParseXml() throws ParseException {

		// Given 
		String externalIdForDocument = generateUUID();
		Date from = DateUtil.makeDanishDateTime(2019, 0, 6, 8, 2, 0);
	    Date to = DateUtil.makeDanishDateTime(2019, 0, 10, 8, 15, 0);
		PHMRDocument phmrDocument = phmrDocumentFactory.defineAsCDA(externalIdForDocument, from, to);
		String xmlDocument = codec.encode(phmrDocument);
		
		// When
		PHMRDocument phmrDocumentParsed = codec.decode(xmlDocument);
		
		// Then		
		assertNotNull(phmrDocumentParsed);
		assertNotNull(phmrDocumentParsed.getId().getRoot());
		assertNotNull(phmrDocumentParsed.getId().getExtension());
		assertNotNull(phmrDocumentParsed.getVitalSignsText());
		assertEquals("Vital Signs", phmrDocumentParsed.getVitalSignsText());
		assertNotNull(phmrDocumentParsed.getVitalSigns());
		assertEquals(3, phmrDocumentParsed.getVitalSigns().size());
		assertNotNull(phmrDocumentParsed.getVitalSigns().get(0).getTranslatedCode());
		assertEquals("NPU03011", phmrDocumentParsed.getVitalSigns().get(0).getTranslatedCode());
		
	}

	
	private String generateUUID() {
		return java.util.UUID.randomUUID().toString();
	}


}
