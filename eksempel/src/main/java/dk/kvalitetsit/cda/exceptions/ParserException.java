package dk.kvalitetsit.cda.exceptions;

public class ParserException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public ParserException(String msg, Exception e) {
		super(msg, e);
	}

}
