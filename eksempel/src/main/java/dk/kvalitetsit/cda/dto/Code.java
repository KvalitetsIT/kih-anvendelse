package dk.kvalitetsit.cda.dto;

public class Code {

	String name;
	
	String code;
	
	String codingScheme;

	public Code(){}
	
	public Code(String name, String code, String codingScheme){
		this.name = name;
		this.code = code;
		this.codingScheme = codingScheme;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	public String getCodingScheme() {
		return codingScheme;
	}

	public void setCodingScheme(String codingScheme) {
		this.codingScheme = codingScheme;
	}

	
}
