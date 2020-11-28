package dk.kvalitetsit.cda.dto;

import java.util.Date;

import org.openehealth.ipf.commons.ihe.xds.core.metadata.AvailabilityStatus;
import org.openehealth.ipf.commons.ihe.xds.core.metadata.DocumentEntryType;

import com.fasterxml.jackson.annotation.JsonFormat;

public class CdaMetadata {

	public CdaMetadata(){}
	
	public Code classCode;
	
	public Code formatCode;
	
	public Code healthcareFacilityTypeCode;
	
	public Code practiceSettingCode;

	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssZ")
	public Date submissionTime;
	
	public AvailabilityStatus availabilityStatus;
	
	public DocumentEntryType objectType;

	public String getMimeType() {
		return "text/xml";
	}

	public Code getClassCode() {
		return classCode;
	}

	public void setClassCode(Code classCode) {
		this.classCode = classCode;
	}

	public Code getFormatCode() {
		return formatCode;
	}

	public void setFormatCode(Code formatCode) {
		this.formatCode = formatCode;
	}

	public Code getHealthcareFacilityTypeCode() {
		return healthcareFacilityTypeCode;
	}

	public void setHealthcareFacilityTypeCode(Code healthcareFacilityTypeCode) {
		this.healthcareFacilityTypeCode = healthcareFacilityTypeCode;
	}

	public Date getSubmissionTime() {
		return submissionTime;
	}

	public void setSubmissionTime(Date submissionTime) {
		this.submissionTime = submissionTime;
	}
	
	public Code getPracticeSettingCode() {
		return practiceSettingCode;
	}

	public void setPracticeSettingCode(Code practiceSettingCode) {
		this.practiceSettingCode = practiceSettingCode;
	}

	public AvailabilityStatus getAvailabilityStatus() {
		return availabilityStatus;
	}

	public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) {
		this.availabilityStatus = availabilityStatus;
	}


	public DocumentEntryType getObjectType() {
		return objectType;
	}

	public void setObjectType(DocumentEntryType objectType) {
		this.objectType = objectType;
	}
}
