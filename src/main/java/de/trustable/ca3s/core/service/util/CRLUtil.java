package de.trustable.ca3s.core.service.util;
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 * original package was 
 * 
 * package demo.sts.provider.cert;
 * 
 */


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.security.auth.x500.X500Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.schedule.CertExpiryScheduler;


@Service
public class CRLUtil {

	Logger LOG = LoggerFactory.getLogger(CRLUtil.class);

	@Autowired
	private CertificateRepository certificateRepository;

	@Autowired
	private CertificateUtil certUtil;
	

    /**
     * Downloads CRL from given URL. Supports http, https, ftp and ldap based
     * URLs.
     */
	@Cacheable("CRLs")
    public X509CRL downloadCRL(String crlURL) throws IOException,
            CertificateException, CRLException, NamingException {
		
		long startTime = System.currentTimeMillis();

		X509CRL crl = null;
        if (crlURL.startsWith("http://") || crlURL.startsWith("https://")
                || crlURL.startsWith("ftp://")) {
        	crl = downloadCRLFromWeb(crlURL);
        } else if (crlURL.startsWith("ldap://")) {
        	crl = downloadCRLFromLDAP(crlURL);
        }
        
        if(crl == null) {
            throw new IOException(
                    "Can not download CRL from certificate "
                            + "distribution point: " + crlURL);
        }
        
        int nRevCerts = 0;
        if( crl.getRevokedCertificates() != null ) {
        	nRevCerts = crl.getRevokedCertificates().size();
        }
		LOG.info("download from '{}' with #{} items took {} mSec", crlURL, nRevCerts, System.currentTimeMillis() - startTime );

        X500Principal principal = crl.getIssuerX500Principal();
        
        List<Certificate> certList = certificateRepository.findCACertByIssuer(principal.getName());
        if( certList.size() == 0) {
        	LOG.debug("principal '{}' not found to verify CRL '{}'", principal.getName(), crlURL);
        	return null;
        }
        
        for( Certificate cert: certList) {
        	try {
	        	X509Certificate x509cert = certUtil.convertPemToCertificate(cert.getContent());
	        	crl.verify(x509cert.getPublicKey());
	            return crl;
        	} catch(GeneralSecurityException gse) {
            	LOG.debug("principal '{}' / cert id {} does NOT verify CRL '{}'", principal.getName(), cert.getId(), crlURL);
        	}
        }

        return null;
    }

    /**
     * Downloads a CRL from given LDAP url, e.g.
     * ldap://ldap.infonotary.com/dc=identity-ca,dc=infonotary,dc=com
     * @throws IOException 
     */
    private X509CRL downloadCRLFromLDAP(String ldapURL) throws CertificateException, 
    NamingException, CRLException, IOException {
    	
    	LOG.debug("loading CRL from LDAP {}", ldapURL);
    	
        Map<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapURL);

        DirContext ctx = new InitialDirContext((Hashtable)env);
        Attributes avals = ctx.getAttributes("");
        Attribute aval = avals.get("certificateRevocationList;binary");
        if(aval == null) {
            throw new IOException(
                    "Can not download CRL from: " + ldapURL);
        }
        byte[] val = (byte[]) aval.get();
        if ((val == null) || (val.length == 0)) {
            throw new IOException(
                    "Can not download CRL from: " + ldapURL);
        } else {
            InputStream inStream = new ByteArrayInputStream(val);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509CRL) cf.generateCRL(inStream);
        }
    }

    /**
     * Downloads a CRL from given HTTP/HTTPS/FTP URL, e.g.
     * http://crl.infonotary.com/crl/identity-ca.crl
     */
    private X509CRL downloadCRLFromWeb(String crlURL) throws MalformedURLException,
    IOException, CertificateException,
            CRLException {
    	
    	LOG.debug("loading CRL from URL {}", crlURL);
    	
        URL url = new URL(crlURL);
        InputStream crlStream = url.openStream();
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509CRL) cf.generateCRL(crlStream);
        } finally {
            crlStream.close();
        }
    }


}