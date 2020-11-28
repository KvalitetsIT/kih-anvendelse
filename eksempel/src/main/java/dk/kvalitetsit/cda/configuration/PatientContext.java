package dk.kvalitetsit.cda.configuration;

public class PatientContext {

	private String patientId;
	
	public PatientContext(String patientId) {
		this.patientId = patientId;
	}

	public String getPatientId() {
		return patientId;
	}

	public void setPatientId(String patientId) {
		this.patientId = patientId;
	}
}
