package dk.kvalitetsit.cda.dgws;

import java.util.List;

import org.apache.cxf.binding.soap.SoapHeader;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;

public class DisableMustUnderstandInterceptor extends AbstractSoapInterceptor {

	public DisableMustUnderstandInterceptor() {
		super(Phase.WRITE);
	}

	@Override
	public void handleMessage(SoapMessage message) throws Fault {
		List<Header> headers = message.getHeaders();
		for (Header header : headers) {
			if (header instanceof SoapHeader) {
				SoapHeader soapHeader = (SoapHeader) header;
				soapHeader.setMustUnderstand(false);
			}
		}
	}

}
