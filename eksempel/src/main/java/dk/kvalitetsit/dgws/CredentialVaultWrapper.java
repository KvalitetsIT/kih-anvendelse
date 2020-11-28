package dk.kvalitetsit.dgws;

/*
 * The MIT License
 *
 * Original work sponsored and donated by National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
 *
 * Copyright (C) 2011 National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import dk.sosi.seal.vault.CredentialPair;
import dk.sosi.seal.vault.CredentialVault;
import dk.sosi.seal.vault.CredentialVaultException;


/**
 * This {@link CredentialVault} is a simple wrapper of a {@link X509Certificate} and a {@link PrivateKey}.
 * 
 * @author Jacob Qvortrup
 */
public class CredentialVaultWrapper implements CredentialVault {

	private X509Certificate certificate;
	private PrivateKey privateKey;

	/**
	 * Wrap the provided certificate and private key in a credential vault
	 */
	public CredentialVaultWrapper(X509Certificate certificate, PrivateKey privateKey) {
		this.certificate = certificate;
		this.privateKey = privateKey;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isTrustedCertificate(X509Certificate certificate) throws CredentialVaultException {
		return this.certificate.equals(certificate);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CredentialPair getSystemCredentialPair() throws CredentialVaultException {
		return new CredentialPair(certificate, privateKey);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setSystemCredentialPair(CredentialPair credentialPair) throws CredentialVaultException {
		this.certificate = credentialPair.getCertificate();
		this.privateKey = credentialPair.getPrivateKey();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public KeyStore getKeyStore() {
		throw new UnsupportedOperationException("No keystore in " + getClass().getSimpleName());
	}

}