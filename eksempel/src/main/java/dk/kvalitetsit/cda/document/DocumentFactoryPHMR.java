package dk.kvalitetsit.cda.document;

import java.util.Date;

import dk.s4.hl7.cda.codes.MedCom;
import dk.s4.hl7.cda.codes.NPU;
import dk.s4.hl7.cda.codes.UCUM;
import dk.s4.hl7.cda.model.AddressData.Use;
import dk.s4.hl7.cda.model.CodedValue.CodedValueBuilder;
import dk.s4.hl7.cda.model.DataInputContext;
import dk.s4.hl7.cda.model.ID;
import dk.s4.hl7.cda.model.OrganizationIdentity;
import dk.s4.hl7.cda.model.Participant.ParticipantBuilder;
import dk.s4.hl7.cda.model.Patient;
import dk.s4.hl7.cda.model.PersonIdentity;
import dk.s4.hl7.cda.model.ReferenceRange;
import dk.s4.hl7.cda.model.phmr.Measurement;
import dk.s4.hl7.cda.model.phmr.MedicalEquipment;
import dk.s4.hl7.cda.model.phmr.PHMRDocument;
import dk.s4.hl7.cda.model.util.DateUtil;

public class DocumentFactoryPHMR {
	
	public PHMRDocument defineAsCDA(String identification, Date from, Date to) {
		//Example is from builder/parser: SetupMedcomKOLExample1
		
	    // Define the 'time'
	    Date documentCreationTime = DateUtil.makeDanishDateTimeWithTimeZone(2020, 5, 9, 14, 50, 10);

	    // 1. Create a PHMR document as a "Green CDA", that is,
	    // a data structure containing only the dynamic data
	    // of a CDA.
	    ID idHeader = new ID.IDBuilder()
	        .setAuthorityName(MedCom.ROOT_AUTHORITYNAME)
	        .setExtension(identification)
	        .setRoot(MedCom.ROOT_OID)
	        .build();
	    PHMRDocument cda = new PHMRDocument(idHeader);

	    Patient nancy = DocumentSetup.defineNancyAsFullPersonIdentity();
	    cda.setPatient(nancy);

	    cda.setTitle("Hjemmemonitorering for " + nancy.getIdValue());
	    cda.setLanguageCode("da-DK");

	    // 1.1 Populate with time and version info
	    cda.setEffectiveTime(documentCreationTime);

	    PersonIdentity andersAndersen = new PersonIdentity.PersonBuilder("Andersen").addGivenName("Anders").build();

	    OrganizationIdentity svendborgHjerteMedicinskAfdeling = new OrganizationIdentity.OrganizationBuilder()
	        .setSOR("241301000016007")
	        .setName("Odense Universitetshospital - Svendborg Sygehus")
	        .setAddress(DocumentSetup.defineHjerteMedicinskAfdAddress())
	        .addTelecom(Use.WorkPlace, "tel", "65112233")
	        .build();

	    OrganizationIdentity organization = new OrganizationIdentity.OrganizationBuilder()
	        .setSOR("241301000016007")
	        .setName("Odense Universitetshospital - Svendborg Sygehus")
	        .build();

	    // 1.3 Populate with Author, Custodian, and Authenticator
	    // Setup Svendborg sygehus Hjertemedicinsk B as organization
	    cda.setAuthor(new ParticipantBuilder()
	        .setAddress(DocumentSetup.defineHjerteMedicinskAfdAddress())
	        .setTelecomList(svendborgHjerteMedicinskAfdeling.getTelecomList())
	        .setSOR(svendborgHjerteMedicinskAfdeling.getIdValue())
	        .setTime(documentCreationTime)
	        .setPersonIdentity(andersAndersen)
	        .setOrganizationIdentity(organization)
	        .build());
	    cda.setCustodian(svendborgHjerteMedicinskAfdeling);
	    Date at1000onJan13 = DateUtil.makeDanishDateTimeWithTimeZone(2020, 5, 9, 14, 50, 10);
	    cda.setLegalAuthenticator(new ParticipantBuilder()
	        .setAddress(DocumentSetup.defineHjerteMedicinskAfdAddress())
	        .setTelecomList(svendborgHjerteMedicinskAfdeling.getTelecomList())
	        .setSOR(svendborgHjerteMedicinskAfdeling.getIdValue())
	        .setTime(at1000onJan13)
	        .setPersonIdentity(andersAndersen)
	        .setOrganizationIdentity(organization)
	        .build());

	    // 1.4 Define the service period
	    cda.setDocumentationTimeInterval(from, to);
	    
	    // 1.5 Add measuring equipment
	    MedicalEquipment medicalEquipment = new MedicalEquipment.MedicalEquipmentBuilder()
	        .setMedicalDeviceCode("MCI00005")
	        .setMedicalDeviceDisplayName("Lung Monitor")
	        .setManufacturerModelName("Manufacturer: Vitalograph / Model: Lung Monitor Bluetooth")
	        .setSoftwareName("SerialNr: N/I / SW Rev. N/I")
	        .build();
	    cda.addMedicalEquipment(medicalEquipment);


	    // 1.6 Add measurements (observations)

	    // Example 1: Use the helper methods to easily create measurements
	    // for commonly used telemedical measurements, here examplified
	    // by weight. Note - no codes, displaynames, nor UCUM units are
	    // given.
	    ID id = new ID.IDBuilder()
	        .setAuthorityName(MedCom.ROOT_AUTHORITYNAME)
	        .setExtension(identification)
	        .setRoot(MedCom.ROOT_OID)
	        .build();

	    Date time1 = DateUtil.makeDanishDateTimeWithTimeZone(2020, 5, 9, 12, 10, 10);
	    DataInputContext context = new DataInputContext(DataInputContext.ProvisionMethod.Electronically,
	        DataInputContext.PerformerType.Citizen);
	    Measurement sat1 = NPU.createSaturation("0.97", time1, context, id);
	    cda.addVitalSign(sat1);

	    // Use the basic methods that allow any legal
	    // code system to be used but requires all data to be
	    // provided
	    Date time2 = DateUtil.makeDanishDateTimeWithTimeZone(2020, 5, 9, 12, 15, 10);
	    Measurement sat2 = new Measurement.MeasurementBuilder(time2, Measurement.Status.COMPLETED)
	        .setPhysicalQuantity("0.92", UCUM.NA, NPU.SATURATION_CODE, NPU.SATURATION_DISPLAYNAME)
	        .setContext(context)
	        .setId(id)
	        .addReferenceRange(new ReferenceRange.ReferenceRangeBuilder()
	            .setCode(new CodedValueBuilder()
	                .setCode(MedCom.DK_OBSERVATION_RANGE_RED_ALERT)
	                .setCodeSystem(MedCom.MESSAGECODE_OID)
	                .setDisplayName(MedCom.DK_OBSERVATION_RANGE_RED_ALERT_DISPLAYNAME)
	                .setCodeSystemName(MedCom.MESSAGECODE_DISPLAYNAME)
	                .build())
	            .setLowValue("0.88", true)
	            .setHighValue(null, false)
	            .build())
	        .addReferenceRange(new ReferenceRange.ReferenceRangeBuilder()
	            .setCode(new CodedValueBuilder()
	                .setCode(MedCom.DK_OBSERVATION_RANGE_YELLOW_ALERT)
	                .setCodeSystem(MedCom.MESSAGECODE_OID)
	                .setDisplayName(MedCom.DK_OBSERVATION_RANGE_YELLOW_ALERT_DISPLAYNAME)
	                .setCodeSystemName(MedCom.MESSAGECODE_DISPLAYNAME)
	                .build())
	            .setLowValue("0.92", true)
	            .setHighValue(null, false)
	            .build())
	        .build();
	    cda.addVitalSign(sat2);

	    Date time3 = DateUtil.makeDanishDateTimeWithTimeZone(2020, 5, 9, 12, 30, 10);
	    Measurement sat3 = new Measurement.MeasurementBuilder(time3, Measurement.Status.COMPLETED)
	        .setPhysicalQuantity("0.95", UCUM.NA, NPU.SATURATION_CODE, NPU.SATURATION_DISPLAYNAME)
	        .setContext(context)
	        .setId(id)
	        .build();
	    cda.addVitalSign(sat3);

	    return cda;
	  }


}
