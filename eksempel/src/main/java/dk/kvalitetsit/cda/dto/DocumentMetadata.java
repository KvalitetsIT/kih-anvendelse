package dk.kvalitetsit.cda.dto;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonFormat;

public class DocumentMetadata extends CdaMetadata {

	public Code patientId;

	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssZ")
	public Date reportTime;
	
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssZ")
	public Date serviceStartTime;	

	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssZ")
	public Date serviceStopTime;	

	public Code organisation;

	public Code typeCode;
	
	public List<Code> eventCodes;
	
	public String languageCode;
	
	public String title;
	
	public Code contentTypeCode;
	
	private Code confidentialityCode;
	
	public String uniqueId;
	
	public Person legalAuthenticator;
	
	public Code sourcePatientId;
	
	public Person sourcePatientInfoPerson;
	
	public String sourcePatientInfoGender;
	
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssZ")
	public Date sourcePatientInfoDateOfBirth;
	
	public Person authorPerson;

	
	public Code getPatientId() {
		return patientId;
	}

	public void setPatientId(Code patientId) {
		this.patientId = patientId;
	}

	public Date getReportTime() {
		return reportTime;
	}
	
	public String getReportTimeStringUTC() {
		if (this.reportTime != null) {
			return dateToStringTimeZone(this.reportTime, "UTC");
		}
		return null;
	}

	public void setReportTime(Date reportTime) {
		this.reportTime = reportTime;
	}

	public Code getOrganisation() {
		return organisation;
	}

	public void setOrganisation(Code organisation) {
		this.organisation = organisation;
	}

	public List<Code> getEventCodes() {
		return eventCodes;
	}

	public void setEventCodes(List<Code> eventCodes) {
		this.eventCodes = eventCodes;
	}

	public String getLanguageCode() {
		return languageCode;
	}

	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Code getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(Code typeCode) {
		this.typeCode = typeCode;
	}

	public Code getConfidentialityCode() {
		return confidentialityCode;
	}

	public void setConfidentialityCode(Code confidentialityCode) {
		this.confidentialityCode = confidentialityCode;
	}
	
	public Date getServiceStartTime() {
		return serviceStartTime;
	}
	
	public String getServiceStartTimeStringUTC() {
		if (this.serviceStartTime != null) {
			return dateToStringTimeZone(this.serviceStartTime, "UTC");
		}
		return null;
	}

	public void setServiceStartTime(Date serviceStartTime) {
		this.serviceStartTime = serviceStartTime;
	}

	public void setServiceStopTime(Date serviceStopTime) {
		this.serviceStopTime = serviceStopTime;
	}

	public Date getServiceStopTime() {
		return this.serviceStopTime;
	}
	
	public String getServiceStopTimeStringUTC() {
		if (this.serviceStopTime != null) {
			return dateToStringTimeZone(this.serviceStopTime, "UTC");
		}
		return null;
	}
	
	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}
	public Person getLegalAuthenticator() {
		return legalAuthenticator;
	}

	public void setLegalAuthenticator(Person legalAuthenticator) {
		this.legalAuthenticator = legalAuthenticator;
	}

	public Code getSourcePatientId() {
		return sourcePatientId;
	}

	public void setSourcePatientId(Code sourcePatientId) {
		this.sourcePatientId = sourcePatientId;
	}

	public Person getSourcePatientInfoPerson() {
		return sourcePatientInfoPerson;
	}

	public void setSourcePatientInfoPerson(Person sourcePatientInfoPerson) {
		this.sourcePatientInfoPerson = sourcePatientInfoPerson;
	}

	public String getSourcePatientInfoGender() {
		return sourcePatientInfoGender;
	}

	public void setSourcePatientInfoGender(String sourcePatientInfoGender) {
		this.sourcePatientInfoGender = sourcePatientInfoGender;
	}


	public Date getSourcePatientInfoDateOfBirth() {
		return sourcePatientInfoDateOfBirth;
	}

	public void setSourcePatientInfoDateOfBirth(Date sourcePatientInfoDateOfBirth) {
		this.sourcePatientInfoDateOfBirth = sourcePatientInfoDateOfBirth;
	}
	
	
	public String getSourcePatientInfoDateOfBirthString() {
		return dateToStringTimeZone(this.sourcePatientInfoDateOfBirth, null).substring(0,  8);
	}
	
	private String dateToStringTimeZone(Date date, String timeZone) {
		DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		if (timeZone != null) {
			formatter.setTimeZone(TimeZone.getTimeZone(timeZone));	
		}
		return formatter.format(date);
	}
	
	public Person getAuthorPerson() {
		return authorPerson;
	}

	public void setAuthorPerson(Person authorPerson) {
		this.authorPerson = authorPerson;
	}

}
