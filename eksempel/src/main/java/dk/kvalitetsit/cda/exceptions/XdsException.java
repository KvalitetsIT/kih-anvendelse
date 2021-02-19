package dk.kvalitetsit.cda.exceptions;

import java.util.LinkedList;
import java.util.List;

import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rs.RegistryError;
import org.openehealth.ipf.commons.ihe.xds.core.stub.ebrs30.rs.RegistryErrorList;

public class XdsException extends Exception {

	private static final long serialVersionUID = 1L;

	List<String> errors = new LinkedList<String>();

	public XdsException() {
	}

	public XdsException(RegistryErrorList registryErrorList) {
		for (RegistryError error : registryErrorList.getRegistryError()) {
			addError("Error received from registry [errorCode:"+error.getErrorCode()+", errorValue:"+error.getValue()+ ", codeContext:" + error.getCodeContext() + "]");
		}
	}

	public String addError(String error) {
		this.errors.add(error);
		return error;
	}

	public List<String> getErrors() {
		return errors;
	}

	public void setErrors(List<String> errors) {
		this.errors = errors;
	}
}
