package dk.kvalitetsit.cda.document;

import java.util.Calendar;

import dk.s4.hl7.cda.model.AddressData;
import dk.s4.hl7.cda.model.Patient;
import dk.s4.hl7.cda.model.Patient.PatientBuilder;

public class DocumentSetup {

	  public static Patient defineNancyAsFullPersonIdentity() {
		    PatientBuilder nancyBuilder = new Patient.PatientBuilder("Berggren")
		        .setGender(Patient.Gender.Female)
		        .addGivenName("Nancy")
		        .addGivenName("Ann")
		        .setSSN("2512489996")
		        .setBirthTime(1948, Calendar.DECEMBER, 25)
		        .setAddress(defineNancyAddress())
		        .addTelecom(AddressData.Use.HomeAddress, "tel", "65123456")
		        .addTelecom(AddressData.Use.WorkPlace, "mailto", "nab@udkantsdanmark.dk");

		    return nancyBuilder.build();
		  }
	  public static AddressData defineNancyAddress() {
		    AddressData nancyAddress = new AddressData.AddressBuilder("5700", "Svendborg")
		        .setCountry("Danmark")
		        .addAddressLine("Skovvejen 12")
		        .addAddressLine("Landet")
		        .setUse(AddressData.Use.HomeAddress)
		        .build();
		    return nancyAddress;
		  }
	  
	  public static AddressData defineHjerteMedicinskAfdAddress() {
		    AddressData hjertemedicinskAddress = new AddressData.AddressBuilder("5700", "Svendborg")
		        .addAddressLine("Hjertemedicinsk afdeling B")
		        .addAddressLine("Valdemarsgade 53")
		        .setCountry("Danmark")
		        .setUse(AddressData.Use.WorkPlace)
		        .build();
		    return hjertemedicinskAddress;
		  }
	
}
