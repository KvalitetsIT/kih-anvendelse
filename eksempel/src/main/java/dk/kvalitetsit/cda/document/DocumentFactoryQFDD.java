package dk.kvalitetsit.cda.document;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;

import dk.s4.hl7.cda.codes.IntervalType;
import dk.s4.hl7.cda.codes.MedCom;
import dk.s4.hl7.cda.model.AddressData.Use;
import dk.s4.hl7.cda.model.CodedValue;
import dk.s4.hl7.cda.model.ID.IDBuilder;
import dk.s4.hl7.cda.model.OrganizationIdentity;
import dk.s4.hl7.cda.model.OrganizationIdentity.OrganizationBuilder;
import dk.s4.hl7.cda.model.Participant.ParticipantBuilder;
import dk.s4.hl7.cda.model.Patient;
import dk.s4.hl7.cda.model.PersonIdentity;
import dk.s4.hl7.cda.model.PersonIdentity.PersonBuilder;
import dk.s4.hl7.cda.model.Section;
import dk.s4.hl7.cda.model.qfdd.QFDDCriterion.QFDDCriterionBuilder;
import dk.s4.hl7.cda.model.qfdd.QFDDDocument;
import dk.s4.hl7.cda.model.qfdd.QFDDFeedback.QFDDFeedbackBuilder;
import dk.s4.hl7.cda.model.qfdd.QFDDHelpText.QFDDHelpTextBuilder;
import dk.s4.hl7.cda.model.qfdd.QFDDMultipleChoiceQuestion.QFDDMultipleChoiceQuestionBuilder;
import dk.s4.hl7.cda.model.qfdd.QFDDOrganizer;
import dk.s4.hl7.cda.model.qfdd.QFDDOrganizer.QFDDOrganizerBuilder;
import dk.s4.hl7.cda.model.qfdd.QFDDPrecondition;
import dk.s4.hl7.cda.model.qfdd.QFDDPrecondition.QFDDPreconditionBuilder;
import dk.s4.hl7.cda.model.qfdd.QFDDQuestion;
import dk.s4.hl7.cda.model.qfdd.QFDDTextQuestion;
import dk.s4.hl7.cda.model.qfdd.QFDDTextQuestion.QFDDTextQuestionBuilder;
import dk.s4.hl7.cda.model.util.DateUtil;

public class DocumentFactoryQFDD {
//TODO: consider more question types
	public QFDDDocument defineAsCDA(String identification, Date from, Date to) throws IOException, URISyntaxException {
		// Examples are from builder/parser. More examples of other type of questions
		// can be found there.
		QFDDDocument cda = createBaseQFDDDocument(identification);

		String text = "OM DETTE SKEMA: "
				+ "Vi bruger blandt andet dine svar til at vurdere, om du har brug for en konsultation. <br/>"
				+ "Hvornår havde du dit seneste anfald?";

		Section<QFDDOrganizer> section = new Section<QFDDOrganizer>("Indledning", text);
		section.addOrganizer(new QFDDOrganizerBuilder().addQFDDQuestion(simpleQuestionMultipleChoice()).addQFDDQuestion(fullQuestionMultipleChoice(true, simplePrecondition())).build());
		cda.addSection(section);

		return cda;
	}

	private QFDDDocument createBaseQFDDDocument(String documentId) {
		// Define the 'time'
		Date documentCreationTime = DateUtil.makeDanishDateTimeWithTimeZone(2014, 0, 13, 10, 0, 0);

		// Create document
		QFDDDocument qfddDocument = new QFDDDocument(MedCom.createId(documentId));
		qfddDocument.setLanguageCode("da-DK");
		qfddDocument.setTitle("KOL spørgeskema");
		qfddDocument.setDocumentVersion("2358344", 1);
		qfddDocument.setEffectiveTime(documentCreationTime);
		// Create Patient
		Patient nancy = DocumentSetup.defineNancyAsFullPersonIdentity();
		qfddDocument.setPatient(nancy);
		// Create Custodian organization
		OrganizationIdentity custodianOrganization = new OrganizationBuilder().setSOR("88878685")
				.setName("Odense Universitetshospital - Svendborg Sygehus")
				.setAddress(DocumentSetup.defineHjerteMedicinskAfdAddress()).addTelecom(Use.WorkPlace, "tel", "65223344")
				.build();
		qfddDocument.setCustodian(custodianOrganization);

		OrganizationIdentity organization = new OrganizationIdentity.OrganizationBuilder().setSOR("88878685")
				.setName("Odense Universitetshospital - Svendborg Sygehus").build();

		PersonIdentity andersAndersen = new PersonBuilder("Andersen").addGivenName("Anders").build();

		qfddDocument.setAuthor(new ParticipantBuilder().setAddress(custodianOrganization.getAddress())
				.setSOR(custodianOrganization.getIdValue()).setTelecomList(custodianOrganization.getTelecomList())
				.setTime(documentCreationTime).setPersonIdentity(andersAndersen).setOrganizationIdentity(organization)
				.build());

		// 1.4 Define the service period
		Date from = DateUtil.makeDanishDateTimeWithTimeZone(2014, 0, 6, 8, 2, 0);
		Date to = DateUtil.makeDanishDateTimeWithTimeZone(2014, 0, 10, 8, 15, 0);
		qfddDocument.setDocumentationTimeInterval(from, to);
		return qfddDocument;
	}

	private QFDDPrecondition simplePrecondition() {
		CodedValue questionCodedValue = new CodedValue("value1", "value2", "value3", "value4");
		return new QFDDPreconditionBuilder(new QFDDCriterionBuilder(questionCodedValue).setMaximum("20")
				.setMinimum("10").setValueType(IntervalType.IVL_INT).build()).build();
	}

	private QFDDQuestion simpleQuestionMultipleChoice() throws IOException, URISyntaxException {
		return new QFDDMultipleChoiceQuestionBuilder().setInterval(1, 2)
				.setCodeValue(new CodedValue("value1", "value2", "value3", "value4"))
				.setId(MedCom.createId("idExtension1"))
				.addAnswerOption("A1", "Some-ChoiceDomain-OID", "Extremely Limited", "Some-CodeSystem-Name")
				.addAnswerOption("A2", "Some-ChoiceDomain-OID", "Quite a bit Limited", "Some-CodeSystem-Name")
				.addAnswerOption("A3", "Some-ChoiceDomain-OID", "Moderately Limited", "Some-CodeSystem-Name")
				.setQuestion("question").build();
	}

	private QFDDQuestion fullQuestionMultipleChoice(boolean includeAssociatedText, QFDDPrecondition qfddPrecondition)
			throws IOException, URISyntaxException {
		CodedValue questionCodedValue = new CodedValue("value1", "value2", "value3", "value4");
		return new QFDDMultipleChoiceQuestionBuilder().setInterval(1, 2).setCodeValue(questionCodedValue)
				.setId(MedCom.createId("idExtension1"))
				.addAnswerOption("A1", "Some-ChoiceDomain-OID", "Extremely Limited", "Some-CodeSystem-Name")
				.addAnswerOption("A2", "Some-ChoiceDomain-OID", "Quite a bit Limited", "Some-CodeSystem-Name")
				.addAnswerOption("A3", "Some-ChoiceDomain-OID", "Moderately Limited", "Some-CodeSystem-Name")
				.setQuestion("question")
				.setFeedback(new QFDDFeedbackBuilder().feedBackText("feedbacktext1").language("da-DK")
						.addPrecondition(new QFDDPreconditionBuilder(new QFDDCriterionBuilder(questionCodedValue)
								.setMaximum("20").setMinimum("1").setValueType(IntervalType.IVL_INT).build()).build())
						.build())
				.setHelpText(new QFDDHelpTextBuilder().helpText("helptext").language("da-DK").build())
				.addPrecondition(new QFDDPreconditionBuilder(new QFDDCriterionBuilder(questionCodedValue)
						.setMaximum("20").setMinimum("1").setValueType(IntervalType.IVL_INT).build()).build())
				.setAssociatedTextQuestion(createAssociatedTextQuestion(includeAssociatedText, qfddPrecondition))
				.build();
	}

	private QFDDTextQuestion createAssociatedTextQuestion(boolean includeAssociatedText,
			QFDDPrecondition qfddPrecondition) throws IOException, URISyntaxException {

		if (!includeAssociatedText) {
			return null;
		}

		QFDDTextQuestionBuilder builder = new QFDDTextQuestionBuilder();
		builder.setCodeValue(new CodedValue("172.Q", "1.2.208.176.1.5", "172.Q", "Sundhedsdatastyrelsen"));
		builder.setId(new IDBuilder().setRoot("1.2.208.176.1.5").setExtension("172")
				.setAuthorityName("Sundhedsdatastyrelsen").build());
		builder.setQuestion("Associated Question Text");
		builder.setHelpText(
				new QFDDHelpTextBuilder().helpText("Associated Question helptext").language("da-DK").build());
		if (qfddPrecondition != null) {
			builder.addPrecondition(qfddPrecondition);
		}
		return builder.build();

	}

}
