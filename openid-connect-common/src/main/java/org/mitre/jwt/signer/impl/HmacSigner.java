/*******************************************************************************
 * Copyright 2012 The MITRE Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.mitre.jwt.signer.impl;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.mitre.jwt.signer.AbstractJwtSigner;
import org.mitre.jwt.signer.JwsAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * JWT Signer using either the HMAC SHA-256, SHA-384, SHA-512 hash algorithm
 * 
 * @author AANGANES, nemonik
 * 
 */
public class HmacSigner extends AbstractJwtSigner implements InitializingBean {

	public static final String DEFAULT_PASSPHRASE = "changeit";

	public static final JwsAlgorithm DEFAULT_ALGORITHM = JwsAlgorithm.HS256;

	private static Logger logger = LoggerFactory.getLogger(HmacSigner.class);

	private Mac mac;

	private String passphrase = DEFAULT_PASSPHRASE;

	/**
	 * Default constructor
	 */
	public HmacSigner() {
		super(DEFAULT_ALGORITHM);
	}

	/**
	 * Create HMAC singer with default algorithm and passphrase as raw bytes
	 * 
	 * @param passphraseAsRawBytes
	 *            The passphrase as raw bytes
	 */
	public HmacSigner(byte[] passphraseAsRawBytes)
			throws NoSuchAlgorithmException {
		this(DEFAULT_ALGORITHM.getJwaName(), new String(passphraseAsRawBytes,
				Charset.forName("UTF-8")));
	}

	/**
	 * Create HMAC singer with default algorithm and passphrase
	 * 
	 * @param passwordAsRawBytes
	 *            The passphrase as raw bytes
	 */
	public HmacSigner(String passphrase) throws NoSuchAlgorithmException {
		this(DEFAULT_ALGORITHM.getJwaName(), passphrase);
	}

	/**
	 * Create HMAC singer with given algorithm and password as raw bytes
	 * 
	 * @param algorithmName
	 *            The Java standard name of the requested MAC algorithm
	 * @param passphraseAsRawBytes
	 *            The passphrase as raw bytes
	 */
	public HmacSigner(String algorithmName, byte[] passphraseAsRawBytes)
			throws NoSuchAlgorithmException {
		this(algorithmName, new String(passphraseAsRawBytes,
				Charset.forName("UTF-8")));
	}

	/**
	 * Create HMAC singer with given algorithm and passwords
	 * 
	 * @param algorithmName
	 *            The Java standard name of the requested MAC algorithm
	 * @param passphrase
	 *            the passphrase
	 */
	public HmacSigner(String algorithmName, String passphrase) {
		super(JwsAlgorithm.getByJwaName(algorithmName));

		Assert.notNull(passphrase, "A passphrase must be supplied");

		setPassphrase(passphrase);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet(){
		initializeMac();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mitre.jwt.signer.AbstractJwtSigner#generateSignature(java.lang.String
	 * )
	 */
	@Override
	public String generateSignature(String signatureBase) throws NoSuchAlgorithmException {
		
		initializeMac();
		
		if (passphrase == null) {
			throw new IllegalArgumentException("Passphrase cannot be null");
		}

		try {
			mac.init(new SecretKeySpec(getPassphrase().getBytes(), mac
					.getAlgorithm()));

			mac.update(signatureBase.getBytes("UTF-8"));
		} catch (GeneralSecurityException e) {
			logger.error("GeneralSecurityException in HmacSigner.java: ", e);
		} catch (UnsupportedEncodingException e) {
			logger.error("UnsupportedEncodingException in HmacSigner.java: ", e);
		}

		byte[] sigBytes = mac.doFinal();

		String sig = new String(Base64.encodeBase64URLSafe(sigBytes));

		// strip off any padding
		sig = sig.replace("=", "");

		return sig;
	}

	public String getPassphrase() {
		return passphrase;
	}

	public void setPassphrase(byte[] rawbytes) {
		this.setPassphrase(new String(rawbytes, Charset.forName("UTF-8")));
	}

	public void setPassphrase(String passphrase) {
		this.passphrase = passphrase;
	}
	
	private void initializeMac() {
		if (mac == null) {
			try {
				mac = Mac.getInstance(getAlgorithm().getStandardName());
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "HmacSigner [mac=" + mac + ", passphrase=" + passphrase + "]";
	}
}
