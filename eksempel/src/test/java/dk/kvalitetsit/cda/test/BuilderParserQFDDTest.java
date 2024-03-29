package dk.kvalitetsit.cda.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import dk.kvalitetsit.cda.document.DocumentFactoryQFDD;
import dk.kvalitetsit.cda.services.CdaMetaDataFactory;
import dk.s4.hl7.cda.convert.QFDDXmlCodec;
import dk.s4.hl7.cda.model.qfdd.QFDDDocument;
import dk.s4.hl7.cda.model.util.DateUtil;

public class BuilderParserQFDDTest {
	
	CdaMetaDataFactory cdaMetaDataFactory;

	DocumentFactoryQFDD qfddDocumentFactory;
	
	private static QFDDXmlCodec codec = new QFDDXmlCodec();

	@Before
	public void init() {
		cdaMetaDataFactory = new CdaMetaDataFactory();
		qfddDocumentFactory = new DocumentFactoryQFDD();
	}
	

	@Test
	public void testCreateQFDD() throws ParseException, IOException, URISyntaxException {

		// Given 
		String externalIdForDocument = generateUUID();
		Date from = DateUtil.makeDanishDateTimeWithTimeZone(2019, 0, 6, 8, 2, 0);
	    Date to = DateUtil.makeDanishDateTimeWithTimeZone(2019, 0, 10, 8, 15, 0);
		
		// When
		QFDDDocument qfddDocument = qfddDocumentFactory.defineAsCDA(externalIdForDocument, from, to);
		
		// Then
		assertNotNull(qfddDocument);
		assertNotNull(qfddDocument.getId().getRoot());
		assertNotNull(qfddDocument.getId().getExtension());
		assertNotNull(qfddDocument.getSections());
		assertEquals(1, qfddDocument.getSections().size());
		assertNotNull(qfddDocument.getSections().get(0));
		assertNotNull(qfddDocument.getSections().get(0).getSectionInformation());
		assertNotNull(qfddDocument.getSections().get(0).getSectionInformation().getTitle());
		assertEquals("Indledning", qfddDocument.getSections().get(0).getSectionInformation().getTitle());
		assertNotNull(qfddDocument.getSections().get(0).getOrganizers());
		assertEquals(2, qfddDocument.getSections().get(0).getOrganizers().get(0).getQFDDQuestions().size());
		assertNotNull(qfddDocument.getSections().get(0).getOrganizers().get(0).getQFDDQuestions().get(0));
		assertNotNull(qfddDocument.getSections().get(0).getOrganizers().get(0).getQFDDQuestions().get(0).getQuestion());
		assertEquals("question", qfddDocument.getSections().get(0).getOrganizers().get(0).getQFDDQuestions().get(0).getQuestion());
				
	}
	
	@Test
	public void testBuildXml() throws ParseException, IOException, URISyntaxException {

		// Given 
		String externalIdForDocument = generateUUID();
		Date from = DateUtil.makeDanishDateTimeWithTimeZone(2019, 0, 6, 8, 2, 0);
	    Date to = DateUtil.makeDanishDateTimeWithTimeZone(2019, 0, 10, 8, 15, 0);
	    QFDDDocument qfddDocument = qfddDocumentFactory.defineAsCDA(externalIdForDocument, from, to);
		
		// When
		String xmlDocument = codec.encode(qfddDocument);
		
		// Then		
		assertNotNull(xmlDocument);
		assertTrue(xmlDocument.length() > 200);
		assertTrue(xmlDocument.contains("KOL spørgeskema"));
		
	}
	
	@Test
	public void testParseXml() throws ParseException, IOException, URISyntaxException {

		// Given 
		String externalIdForDocument = generateUUID();
		Date from = DateUtil.makeDanishDateTimeWithTimeZone(2019, 0, 6, 8, 2, 0);
	    Date to = DateUtil.makeDanishDateTimeWithTimeZone(2019, 0, 10, 8, 15, 0);
	    QFDDDocument qfddDocument = qfddDocumentFactory.defineAsCDA(externalIdForDocument, from, to);
		String xmlDocument = codec.encode(qfddDocument);
		
		// When
		QFDDDocument qfddDocumentParsed = codec.decode(xmlDocument);
		
		// Then
		assertNotNull(qfddDocumentParsed);
		assertNotNull(qfddDocumentParsed.getId().getRoot());
		assertNotNull(qfddDocumentParsed.getId().getExtension());
		assertNotNull(qfddDocumentParsed.getSections());
		assertEquals(1, qfddDocumentParsed.getSections().size());
		assertNotNull(qfddDocumentParsed.getSections().get(0));
		assertNotNull(qfddDocumentParsed.getSections().get(0).getSectionInformation());
		assertNotNull(qfddDocumentParsed.getSections().get(0).getSectionInformation().getTitle());
		assertEquals("Indledning", qfddDocumentParsed.getSections().get(0).getSectionInformation().getTitle());
		assertNotNull(qfddDocument.getSections().get(0).getOrganizers());
		assertEquals(2, qfddDocument.getSections().get(0).getOrganizers().get(0).getQFDDQuestions().size());
		assertNotNull(qfddDocument.getSections().get(0).getOrganizers().get(0).getQFDDQuestions().get(0));
		assertNotNull(qfddDocument.getSections().get(0).getOrganizers().get(0).getQFDDQuestions().get(0).getQuestion());
		assertEquals("question", qfddDocument.getSections().get(0).getOrganizers().get(0).getQFDDQuestions().get(0).getQuestion());
			
	}

	
	private String generateUUID() {
		return java.util.UUID.randomUUID().toString();
	}


}
