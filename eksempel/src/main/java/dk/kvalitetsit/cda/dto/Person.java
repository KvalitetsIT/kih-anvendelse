package dk.kvalitetsit.cda.dto;

public class Person {
	
	String givenName;
	String familyName;
	String secondAndFurtherGivenNames;
	
	public String getGivenName() {
		return givenName;
	}
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}
	public String getFamilyName() {
		return familyName;
	}
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}
	public String getSecondAndFurtherGivenNames() {
		return secondAndFurtherGivenNames;
	}
	public void setSecondAndFurtherGivenNames(String secondAndFurtherGivenNames) {
		this.secondAndFurtherGivenNames = secondAndFurtherGivenNames;
	}

}
