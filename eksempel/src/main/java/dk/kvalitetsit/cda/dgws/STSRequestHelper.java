package dk.kvalitetsit.cda.dgws;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class STSRequestHelper {
	
	@Value("${sts.url}")
	private String stsUrl;

	@Autowired
	@Qualifier("dgwsTemplate")
	private RestTemplate restTemplate;

	public String sendRequest(String postBody) throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", "text/xml; charset=utf-8");
		headers.set("SOAPAction", "\"Issue\"");

		HttpEntity<String> entity = new HttpEntity<>(postBody, headers);

		System.out.println(stsUrl);
		ResponseEntity<String> result = restTemplate.exchange(stsUrl, HttpMethod.POST, entity, String.class);
		
		int statusCode = result.getStatusCode().value();
		if (statusCode != 200) {
			throw new IOException("HTTP POST failed (" + statusCode + "): " + result.getBody());
		}

		return result.getBody();
	}
}
