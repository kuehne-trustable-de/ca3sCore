package de.trustable.ca3s.core.service.cmp;

import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.CryptoService;
import de.trustable.cmp.client.cmpClient.KeystoreSigner;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.cmp.CMPException;
import org.bouncycastle.cert.cmp.ProtectedPKIMessage;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;

public class VerifyingKeystoreSigner extends KeystoreSigner {

    Logger LOGGER = LoggerFactory.getLogger(VerifyingKeystoreSigner.class);

    private final CertificateUtil certificateUtil;

    public VerifyingKeystoreSigner(KeyStore ks, String ksAlias, String ksSecret, final boolean ignoreFailedVerification, CertificateUtil certificateUtil) throws KeyStoreException {
        super(ks, ksAlias, ksSecret, ignoreFailedVerification);
        this.certificateUtil = certificateUtil;
    }

    @Override
    public boolean verifyMessage(ProtectedPKIMessage message) throws GeneralSecurityException {

        LOGGER.debug("in KeystoreSigner.verifyMessage ...");

        try {
            if (message.hasPasswordBasedMacProtection()) {
                throw new GeneralSecurityException("Server used MacProtection, but certificate & key present!");
            }
        }catch( Exception ex){
            if(isIgnoreFailedVerification()) {
                LOGGER.info("hasPasswordBasedMacProtection causes exception", ex);
            }else{
                throw ex;
            }
        }

        for(X509CertificateHolder x509CertificateHolder: message.getCertificates()) {

            try {
                Certificate certificate = certificateUtil.createCertificate(x509CertificateHolder.getEncoded(),
                    null, null, false,
                    "");

                if (certificate.isEndEntity()) {

                    try {
                        X509Certificate x509Cert = CryptoService.convertPemToCertificate(certificate.getContent());
                        ContentVerifierProvider verifierProvider = new JcaContentVerifierProviderBuilder()
                            .setProvider(BouncyCastleProvider.PROVIDER_NAME)
                            .build(x509Cert);

                        if (message.verify(verifierProvider)) {
                            LOGGER.debug("verifyMessage succeeded for certificate #{} ", certificate.getId());

                            if( !certificate.isActive() ){
                                LOGGER.info("certificate #{} NOT active", certificate.getId());
                            }
                            return true;
                        }
                    } catch (CMPException | OperatorCreationException e) {
                        if (isIgnoreFailedVerification()) {
                            LOGGER.info("verification of ProtectedPKIMessage failed", e);
                        } else {
                            throw new GeneralSecurityException(e);
                        }
                    }
                }

                if (isIgnoreFailedVerification()) {
                    LOGGER.info("no matching certificate found for ProtectedPKIMessage verification");
                    return true;
                } else {
                    throw new GeneralSecurityException("no matching certificate found for ProtectedPKIMessage verification");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return false;
    }

}
