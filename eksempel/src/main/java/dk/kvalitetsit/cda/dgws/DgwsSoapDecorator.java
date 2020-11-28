package dk.kvalitetsit.cda.dgws;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.apache.cxf.binding.soap.Soap11;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dk.kvalitetsit.dgws.DgwsContext;
import dk.sosi.seal.SOSIFactory;
import dk.sosi.seal.model.IDCard;
import dk.sosi.seal.model.Request;
import dk.sosi.seal.model.SecurityTokenResponse;
import dk.sosi.seal.model.SystemIDCard;
import dk.sosi.seal.xml.XmlUtil;

public class DgwsSoapDecorator extends AbstractSoapInterceptor {

	@Autowired
	private SOSIFactory sosiFactory;

	@Autowired
	private STSRequestHelper requestHelper;

	@Autowired
	private DgwsContext dgwsContext;

	public DgwsSoapDecorator() {
		super(Phase.PRE_STREAM);
	}

	@Override
	public void handleMessage(SoapMessage message) throws Fault {
		try {
			// DGWS is SOAP11
			message.setVersion(Soap11.getInstance());

			// Add the DGWS headers
			Document sosi = getSosiDocument();
			NodeList children = sosi.getDocumentElement().getFirstChild().getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node element = children.item(i);
				QName qname = new QName(element.getNamespaceURI(), element.getLocalName());
				Header dgwsHeader = new Header(qname, element);
				message.getHeaders().add(dgwsHeader);
			}
			
		} catch (IOException e) {
			throw new Fault(e);
		}
	}

	private Document getSosiDocument() throws IOException {
		Request request = sosiFactory.createNewRequest(false, null);
		request.setIDCard(getToken());
		return request.serialize2DOMDocument();
	}

	private IDCard getToken() throws IOException {

		Document requestXml = dgwsContext.getSosiIdCardRequest();
	
		String requestXmlString = XmlUtil.node2String(requestXml, false, true);
		
		String responseXml = requestHelper.sendRequest(requestXmlString);
		SecurityTokenResponse securityTokenResponse = sosiFactory.deserializeSecurityTokenResponse(responseXml);

		if (securityTokenResponse.isFault() || securityTokenResponse.getIDCard() == null) {
			throw new RuntimeException("No ID card :-(");
		}
		else {
			SystemIDCard stsSignedIdCard = (SystemIDCard) securityTokenResponse.getIDCard();
			return stsSignedIdCard;
		}
	}

}
