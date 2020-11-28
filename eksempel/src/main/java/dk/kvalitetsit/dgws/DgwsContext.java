package dk.kvalitetsit.dgws;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;

import dk.kvalitetsit.cda.configuration.PatientContext;
import dk.nsi.hsuid._2016._08.hsuid_1_1.SubjectIdentifierType;
import dk.sosi.seal.SOSIFactory;
import dk.sosi.seal.model.AuthenticationLevel;
import dk.sosi.seal.model.CareProvider;
import dk.sosi.seal.model.SecurityTokenRequest;
import dk.sosi.seal.model.SignatureConfiguration;
import dk.sosi.seal.model.SignatureUtil;
import dk.sosi.seal.model.SystemIDCard;
import dk.sosi.seal.model.UserIDCard;
import dk.sosi.seal.model.UserInfo;
import dk.sosi.seal.model.constants.IDValues;
import dk.sosi.seal.model.constants.SubjectIdentifierTypeValues;
import dk.sosi.seal.pki.SignatureProviderFactory;
import dk.sosi.seal.vault.CredentialVault;

public class DgwsContext {

	@Autowired
	private SOSIFactory sosiFactory;

	@Autowired
	private CredentialVault defaultVault;

	@Value("${medcom.cvr}")
	private String cvr;

	@Value("${medcom.orgname}")
	private String orgname;

	@Value("${medcom.itsystem}")
	private String itsystem;

	protected PatientContext uc;

	protected CredentialVault userVault;
	protected UserInfo userInfo;
	protected CareProvider careProvider;
	
	protected Boolean consentOverride = true;
	
	
	public DgwsContext(PatientContext uc) {
		this.uc = uc;
	}

	public PatientContext getPatientContext() {
		return uc;
	}

	public void setPatientContext(PatientContext uc) {
		this.uc = uc;
	}
	
	public Document getSosiIdCardRequest() throws IOException {
		
		if (this.userVault != null && this.userInfo != null) {
			UserIDCard selfSignedUserIdCard = sosiFactory.createNewUserIDCard("Test", this.userInfo, this.careProvider, AuthenticationLevel.MOCES_TRUSTED_USER, null, null, userVault.getSystemCredentialPair().getCertificate(), null);

			SecurityTokenRequest securityTokenRequest = sosiFactory.createNewSecurityTokenRequest();
			securityTokenRequest.setIDCard(selfSignedUserIdCard);
			Document doc = securityTokenRequest.serialize2DOMDocument();

			SignatureConfiguration signatureConfiguration = new SignatureConfiguration(new String[] { IDValues.IDCARD }, IDValues.IDCARD, IDValues.id);
			SignatureUtil.sign(SignatureProviderFactory.fromCredentialVault(userVault), doc, signatureConfiguration);

			return doc;
			
		} else {
			CareProvider careProvider = new CareProvider(SubjectIdentifierTypeValues.CVR_NUMBER, cvr, orgname);
			SystemIDCard selfSignedSystemIdCard = sosiFactory.createNewSystemIDCard(itsystem, careProvider, AuthenticationLevel.VOCES_TRUSTED_SYSTEM, null, null, defaultVault.getSystemCredentialPair().getCertificate(), null);
			
			SecurityTokenRequest securityTokenRequest = sosiFactory.createNewSecurityTokenRequest();
			securityTokenRequest.setIDCard(selfSignedSystemIdCard);

			Document doc = securityTokenRequest.serialize2DOMDocument();
			return doc;
		}
	}

	
	public void setDgwsUserContext(Resource keystore, String password, String alias, UserInfo userInfo, CareProvider careProvider, boolean consentOverride) throws IOException {
		this.userVault = getVaultFromFile(keystore, password, alias);
		this.userInfo = userInfo;
		this.careProvider = careProvider;
		this.consentOverride = consentOverride;
	}
	
	public void clearDgwsUserContext() {
		this.userVault = null;
		this.userInfo = null;
		this.careProvider = null;
		this.consentOverride = true;
	}

	
	public String getUserCivilRegistrationNumber() {
		if (userInfo != null) {
			return userInfo.getCPR();
		} else {
			return "0101584160";			
		}
	}

	public String getUserAuthorizationCode() {
		if (userInfo != null) {
			return userInfo.getAuthorizationCode();
		} else {
			return "SRTTQ";
		}
	}

	public String getConsentOverride() {
		return consentOverride.toString();
	}

	public CredentialVault getVaultFromFile(Resource r, String password, String alias) throws IOException {
		try {
			InputStream in = r.getInputStream();
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(in, password.toCharArray());
			X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
			PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password.toCharArray());
			return new CredentialVaultWrapper(certificate, privateKey);
		} catch (KeyStoreException e) {
			throw new IOException("Error loading certificate and private key from " + r, e);
		} catch (NoSuchAlgorithmException e) {
			throw new IOException("Error loading certificate and private key from " + r, e);
		} catch (CertificateException e) {
			throw new IOException("Error loading certificate and private key from " + r, e);
		} catch (UnrecoverableKeyException e) {
			throw new IOException("Error loading certificate and private key from " + r, e);
		}
	}

	public String getOrganisationIdentifier() {
		if (careProvider != null) {
			return careProvider.getID();
		}
		return "293591000016003";
	}

	public String getOrganisationIdentifierType() {
		if (careProvider != null) {
			return careProvider.getType();
		}
		
		return SubjectIdentifierType.NSI_SORCODE.toString();
	}

}
