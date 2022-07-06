package de.trustable.ca3s.core.web.scepservlet;

import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.domain.enumeration.ContentRelationType;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentType;
import de.trustable.ca3s.core.domain.enumeration.ScepOrderStatus;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.repository.ProtectedContentRepository;
import de.trustable.ca3s.core.repository.ScepOrderRepository;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.util.*;
import de.trustable.util.CryptoUtil;
import de.trustable.util.Pkcs10RequestHolder;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.jscep.server.ScepServlet;
import org.jscep.transaction.FailInfo;
import org.jscep.transaction.OperationFailureException;
import org.jscep.transaction.TransactionId;
import org.jscep.transport.response.Capability;
import org.jscep.util.CertificationRequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.servlet.ServletException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.*;

import static de.trustable.ca3s.core.domain.ScepOrderAttribute.ATTRIBUTE_CN;


/**
 * a delegating servlet forwarding the SCEP requests to the backend.
 *
 * @author ak
 *
 */
@Controller
public class ScepServletImpl extends ScepServlet {

    /**
	 *
	 */
	private static final long serialVersionUID = 7773233909179939491L;

	private static final Logger LOGGER = LoggerFactory.getLogger(ScepServletImpl.class);

    private static final X500Name pollName = new X500Name("CN=Poll");

    @Autowired
    CertificateRepository certRepository;

    @Autowired
    CSRRepository csrRepository;

    @Autowired
    ScepOrderRepository scepOrderRepository;

    @Autowired
    private CryptoUtil cryptoUtil;

    @Autowired
    private CertificateUtil certUtil;

    @Autowired
    private CertificateProcessingUtil cpUtil;

    @Autowired
    private ScepOrderUtil scepOrderUtil;

    @Autowired
    private ProtectedContentRepository protectedContentRepository;

    @Autowired
    private ProtectedContentUtil protectedContentUtil;


    final private PipelineUtil pipelineUtil;

	public ThreadLocal<Pipeline> threadLocalPipeline = new ThreadLocal<>();

    public ScepServletImpl(PipelineUtil pipelineUtil) {
        this.pipelineUtil = pipelineUtil;
    }

    @Override
    public void init() throws ServletException {
    }

    /**
     *
     * @return
     * @throws ServletException
     */
    Certificate getCurrentRecepientCert() throws ServletException, OperationFailureException {

        Pipeline pipeline = threadLocalPipeline.get();
        if( pipeline == null ) {
            LOGGER.warn("doEnrol: no processing pipeline defined");
            throw new OperationFailureException(FailInfo.badRequest);
        }

        try {
            return pipelineUtil.getSCEPRecipientCertificate(pipeline, cpUtil);

        } catch (GeneralSecurityException | IOException e) {
            throw new ServletException(e);
        }

    }
/*
    private Certificate createCertificate(final String csrAsPem) {

        CSR csr = csrUtil.buildCSR(csrAsPem, CsrAttribute.REQUESTOR_SYSTEM, csrUtil.parseBase64CSR(csrAsPem), PipelineType.INTERNAL, null);
        CAConnectorConfig caConfig = configUtil.getDefaultConfig();
        return signCertificateRequest(csr, caConfig );

    }
*/

	private Certificate startCertificateCreationProcess(final String csrAsPem, TransactionId transId, Pipeline pipeline, ScepOrder scepOrder) throws OperationFailureException, IOException, GeneralSecurityException {

        Pkcs10RequestHolder p10Holder = cryptoUtil.parseCertificateRequest(csrAsPem);
        if( !pipelineUtil.isPipelineRestrictionsResolved(pipeline, p10Holder, new ArrayList<>())){
            throw new OperationFailureException(FailInfo.badRequest);
        }

        String pipelineName = ( pipeline == null) ? "NoPipeline":pipeline.getName();
        String requestorName = "SCEP client";
        LOGGER.debug("doEnrol: processing request by {} using pipeline {}", requestorName,pipelineName);

		CSR csr = cpUtil.buildCSR(csrAsPem, requestorName, AuditService.AUDIT_SCEP_CERTIFICATE_REQUESTED, "", pipeline );

		CsrAttribute csrAttributeTransId = new CsrAttribute();
		csrAttributeTransId.setName(CertificateAttribute.ATTRIBUTE_SCEP_TRANS_ID);
		csrAttributeTransId.setValue(transId.toString());
		csr.addCsrAttributes(csrAttributeTransId);
		csrRepository.save(csr);

        scepOrder.setCsr(csr);

		Certificate cert = cpUtil.processCertificateRequest(csr, requestorName,  AuditService.AUDIT_SCEP_CERTIFICATE_CREATED, pipeline );

		if( cert == null) {
			LOGGER.warn("creation of certificate by SCEP transaction id '{}' failed ", transId);
		}else {
			LOGGER.debug("new certificate id '{}' for SCEP transaction id '{}'", cert.getId(), transId);

            scepOrder.setCertificate(cert);

			certUtil.setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_SCEP_TRANS_ID, transId.toString());
			certRepository.save(cert);
		}

		return cert;
	}

    @Override
    protected List<X509Certificate> doEnrol(PKCS10CertificationRequest csr,
    		X509Certificate sender,
            TransactionId transId) throws OperationFailureException {

        Pipeline pipeline = threadLocalPipeline.get();
        if( pipeline == null ) {
            LOGGER.warn("doEnrol: no processing pipeline defined");
            throw new OperationFailureException(FailInfo.badRequest);
        }

        LOGGER.debug("doEnrol(" + csr.toString() + ", " + transId.toString() +") using pipeline '{}'", pipeline.getName());

        ScepOrder scepOrder = new ScepOrder();
        scepOrder.setPipeline(pipeline);
        scepOrder.setRealm(pipeline.getUrlPart());
        scepOrder.setTransId(transId.toString());
        scepOrder.setRequestedOn(Instant.now());
        scepOrder.setRequestedBy(sender.getSubjectX500Principal().getName());

        // start with ...
        scepOrder.setStatus(ScepOrderStatus.PENDING);
        scepOrder.setPasswordAuthentication(false);

        scepOrder.setAsyncProcessing(false);

        try {

            X500Name subject = X500Name.getInstance(csr.getSubject());
            LOGGER.debug(subject.toString());
            if (subject.equals(pollName)) {
                return Collections.emptyList();
            }

            scepOrderUtil.setOrderAttribute(scepOrder, ATTRIBUTE_CN, csr.getSubject().toString());
            insertSANs(scepOrder, csr);
            scepOrderRepository.save(scepOrder);

            String password = CertificationRequestUtils.getChallengePassword(csr);
            if( password != null){
                checkPassword(pipeline, password);
                scepOrder.setPasswordAuthentication(true);
            }else{
                Certificate senderCert = certUtil.createCertificate(sender.getEncoded(), null, null, false,
                    "scep sender certificate");

                if( !senderCert.isActive()){
                    LOGGER.warn("certificate {} not active! Revoked {}, expiring on {}", senderCert.getId(), senderCert.isRevoked(), senderCert.getValidTo());
                    scepOrder.setStatus(ScepOrderStatus.INVALID);
                    throw new OperationFailureException(FailInfo.badRequest);
                }

                boolean isTrusted = false;
                List<Certificate> senderChain = certUtil.getCertificateChain(senderCert);
                for( Certificate chainCert: senderChain) {
                    if (!certUtil.getCertAttributes(chainCert, CertificateAttribute.ATTRIBUTE_SCEP_TRUSTED_ISSUER).isEmpty()) {
                        isTrusted = true;
                        LOGGER.debug("certificate {} valid a scep issuer!", chainCert.getId());
                        scepOrder.setAuthenticatedBy(chainCert);
                        break;
                    }
                }
                if(!isTrusted){
                    LOGGER.warn("certificate authentication, no valid issuer found!");
                    scepOrder.setStatus(ScepOrderStatus.INVALID);
                    throw new OperationFailureException(FailInfo.badRequest);
                }
            }

            String p10ReqPem = CryptoUtil.pkcs10RequestToPem(csr);
        	Certificate newCertDao = startCertificateCreationProcess(p10ReqPem, transId, pipeline, scepOrder);
        	if( newCertDao == null ){
                LOGGER.debug("creation of certificate failed");
                scepOrder.setStatus(ScepOrderStatus.INVALID);
                throw new OperationFailureException(FailInfo.badRequest);
        	}

    		certUtil.setCertAttribute(newCertDao, CertificateAttribute.ATTRIBUTE_SCEP_TRANS_ID, transId.toString());

			certRepository.save(newCertDao);

			ArrayList<X509Certificate> certList = new ArrayList<>();
            Certificate chainCert = newCertDao;
            for( int i = 0; i < 3; i++){

                if( chainCert != null) {
                    certList.add(CryptoUtil.convertPemToCertificate(chainCert.getContent()));
                    chainCert = chainCert.getIssuingCertificate();
                }
            }
            for(X509Certificate x509: certList){
                LOGGER.debug("--- chain element: " + x509.getSubjectX500Principal().getName());
            }

            scepOrder.setStatus(ScepOrderStatus.READY);

            return certList;

        } catch (Exception e) {
            LOGGER.warn("Error in enrollment", e);
            scepOrder.setStatus(ScepOrderStatus.INVALID);
            throw new OperationFailureException(FailInfo.badRequest);
        }finally{
            scepOrderRepository.save(scepOrder);
        }
    }

    void checkPassword(final Pipeline pipeline, final String password) throws OperationFailureException{

        if( password == null || password.isEmpty()) {
            LOGGER.warn("password not present in SCEP request / is empty!");
            throw new OperationFailureException(FailInfo.badRequest);
        }

        List<ProtectedContent> listPC = protectedContentRepository.findByTypeRelationId(ProtectedContentType.PASSWORD, ContentRelationType.SCEP_PW,pipeline.getId());
        for(ProtectedContent pc: listPC){
            String expectedPassword = protectedContentUtil.unprotectString(pc.getContentBase64()).trim();
            LOGGER.debug("Pipeline '{}' defined SCEP password '{}'", pipeline.getName(), expectedPassword);
            if( password.trim().equals(expectedPassword)) {
                LOGGER.debug("Protected Content found matching SCEP password");
                return; // the only successful exit !!
            } else {
                LOGGER.debug("Protected Content password does not match SCEP password '{}' != '{}'", expectedPassword, password);
            }
        }

        LOGGER.warn("no (active) password present in pipeline '" + pipeline.getName() + "' !");
        throw new OperationFailureException(FailInfo.badRequest);
    }

    @Override
    protected List<X509Certificate> doGetCaCertificate(String identifier) throws OperationFailureException{
        LOGGER.debug("doGetCaCertificate(" + identifier +")");

        List<X509Certificate> caList = new ArrayList<>();
        try {
			Certificate recepCert = getCurrentRecepientCert();
			caList = certUtil.getX509CertificateChainAsList(recepCert);
		} catch (GeneralSecurityException | ServletException e) {
			LOGGER.warn("Failed to retrieve CA certificates", e);
		}

        return caList;
    }

    @Override
    protected X509CRL doGetCrl(X500Name issuer, BigInteger serial) {
        LOGGER.debug("doGetCrl(" + issuer.toString() +", "+ serial.toString(10) +")");
        return null;
    }

    @Override
    protected Set<Capability> doCapabilities(String identifier) {
        LOGGER.debug("doCapabilities(" + identifier +")");
        return EnumSet.of(Capability.RENEWAL,
        		Capability.SCEP_STANDARD,
        		Capability.SHA_256, Capability.SHA_512,
        		Capability.POST_PKI_OPERATION);
    }

    @Override
    protected List<X509Certificate> doGetCert(X500Name issuer, BigInteger serial)
            throws OperationFailureException {


        LOGGER.debug("doGetCert(" + issuer.toString() +", "+ serial.toString() +")");

    	List<Certificate> certDaoList = certRepository.findByIssuerSerial(issuer.toString(), serial.toString());

    	if( certDaoList.isEmpty()){
            LOGGER.debug("no match for doGetCert(" + issuer +", "+ serial +")");

            RDN[] rdns = issuer.getRDNs();
            for( RDN rdn: rdns){
            	AttributeTypeAndValue[] attTVArr = rdn.getTypesAndValues();
                for( AttributeTypeAndValue attTV: attTVArr){
                    LOGGER.debug("AttributeTypeAndValue of issuer :" + attTV.getType().toString() +" = "+ attTV.getValue().toString() );
                }
            }
            RDN[] rdnsIssuer =  issuer.getRDNs(BCStyle.CN);
            if( rdnsIssuer.length > 0){
	            String rdnIssuerString = rdnsIssuer[0].getFirst().getValue().toString();
                String paddedSerial = CertificateUtil.getPaddedSerial(serial.toString());

                LOGGER.debug("looking for cert('" + rdnIssuerString +"', '"+ paddedSerial +"')");

                certDaoList = certRepository.findBySearchTermNamed1(CertificateAttribute.ATTRIBUTE_SERIAL_PADDED, paddedSerial);

                if( certDaoList.isEmpty()){
                    LOGGER.warn("looking for cert by padded serial '"+ paddedSerial +"' failed, nothing found");
                    throw new OperationFailureException(FailInfo.badCertId);
                } else if( certDaoList.size() > 1){
                    LOGGER.warn("looking for cert by padded serial '"+ paddedSerial +"' failed, multiple certs found");
                    throw new OperationFailureException(FailInfo.badCertId);
                }
            }

    	}
        if( certDaoList.isEmpty()){
            throw new OperationFailureException(FailInfo.badCertId);
        }

        List<X509Certificate> certList = new ArrayList<>();

        for(Certificate certDao: certDaoList ){
        	try {
        		X509Certificate x509Cert = CryptoUtil.convertPemToCertificate(certDao.getContent());
        		if( x509Cert.getIssuerX500Principal().getName().equals(issuer.toString())){
                    LOGGER.debug("issuer match for doGetCert(" + issuer +", "+ serial +")");
        		}
				certList.add(x509Cert);
			} catch (GeneralSecurityException e) {
				LOGGER.warn("decoding certificate failed", e);
	            throw new OperationFailureException(FailInfo.badRequest);
			}
        }

        return certList ;
    }

    @Override
    protected List<X509Certificate> doGetCertInitial(X500Name issuer,
            X500Name subject, TransactionId transId){

        LOGGER.debug("doGetCertInitial(" + issuer.toString() +", "+ subject.toString() + ", " + transId.toString() +")");

        if (subject.equals(pollName)) {
            return Collections.emptyList();
        }
/*
 * not implemented
 *
        try {
            return Collections.singletonList( ...);
        } catch (Exception e) {
            LOGGER.info("doGetCertInitial", e);
            throw new OperationFailureException(FailInfo.badCertId);
        }
*/
        return Collections.emptyList();
    }

    @Override
    protected List<X509Certificate> getNextCaCertificate(String identifier) {
        LOGGER.debug("getNextCaCertificate(" + identifier +")");
/*
        if (identifier == null || identifier.length() == 0) {
            return Collections.singletonList(ca);
        }
*/
        return Collections.emptyList();
    }

    @Override
    protected PrivateKey getRecipientKey() {

        try {
			return certUtil.getPrivateKey(getCurrentRecepientCert());
		} catch (ServletException | OperationFailureException e) {
			LOGGER.warn("problem retrieving recipient's private key", e);
			return null;
		}
    }

    @Override
    protected X509Certificate getRecipient() {

        try {
        	X509Certificate ca = CryptoUtil.convertPemToCertificate(getCurrentRecepientCert().getContent());
	        LOGGER.debug("getRecipient() returns " + ca.toString());
	        return ca;
		} catch (GeneralSecurityException | ServletException | OperationFailureException e) {
			LOGGER.warn("problem retrieving recipient certificate", e);
		}
        return null;
    }

    @Override
    protected PrivateKey getSignerKey() {
        LOGGER.debug("getSignerKey(), returning getRecipientKey()");
        return getRecipientKey();
    }

    @Override
    protected X509Certificate getSigner() {
        LOGGER.debug("getSigner(), returning getRecipient()");
        return getRecipient();
    }

    @Override
    protected X509Certificate[] getSignerCertificateChain() {
        LOGGER.debug("getSignerCertificateChain()");

        X509Certificate[] signerChainArr = new X509Certificate[0];

        try {
			Certificate recepCert = getCurrentRecepientCert();

			List<Certificate> certList = certUtil.getCertificateChain(recepCert);
			int chainLength = certList.size();
			if( chainLength > 1) {
				signerChainArr = new X509Certificate[chainLength-1];

				int j = 0;
				for( int i = 1; i < chainLength; i++) {
					signerChainArr[j++] = CryptoService.convertPemToCertificate(certList.get(i).getContent());
				}

			}
		} catch (GeneralSecurityException | ServletException | OperationFailureException e) {
			LOGGER.warn("Failed to retrieve CA certificates", e);
		}

        return signerChainArr;

    }

    private void insertSANs(ScepOrder scepOrder, final PKCS10CertificationRequest csr){

        Set<GeneralName> generalNameSet = CSRUtil.getSANList(csr.getAttributes());

        String allSans = "";
        LOGGER.debug("putting SANs into ScepOrderAttributes");

        for (GeneralName gName : generalNameSet) {

            String sanValue = gName.getName().toString();
            if (GeneralName.otherName == gName.getTagNo()) {
                sanValue = "--other value--";
            } else if (GeneralName.iPAddress == gName.getTagNo()) {
                sanValue = CertificateUtil.getTypedSAN(gName);
            }
            if( allSans.length() > 0) {
                allSans += ";";
            }
            allSans += sanValue;

            scepOrderUtil.setOrderAttribute(scepOrder, CsrAttribute.ATTRIBUTE_SAN, sanValue, true);
            if (GeneralName.dNSName == gName.getTagNo()) {
                scepOrderUtil.setOrderAttribute(scepOrder, CsrAttribute.ATTRIBUTE_TYPED_SAN, "DNS:" + sanValue, true);
            } else if (GeneralName.iPAddress == gName.getTagNo()) {
                scepOrderUtil.setOrderAttribute(scepOrder, CsrAttribute.ATTRIBUTE_TYPED_SAN, "IP:" + sanValue, true);
            } else if (GeneralName.ediPartyName == gName.getTagNo()) {
                scepOrderUtil.setOrderAttribute(scepOrder, CsrAttribute.ATTRIBUTE_TYPED_SAN, "EDI:" + sanValue, true);
            } else if (GeneralName.otherName == gName.getTagNo()) {
                scepOrderUtil.setOrderAttribute(scepOrder, CsrAttribute.ATTRIBUTE_TYPED_SAN, "other:" + sanValue, true);
            } else if (GeneralName.registeredID == gName.getTagNo()) {
                scepOrderUtil.setOrderAttribute(scepOrder, CsrAttribute.ATTRIBUTE_TYPED_SAN, "regID:" + sanValue, true);
            } else if (GeneralName.rfc822Name == gName.getTagNo()) {
                scepOrderUtil.setOrderAttribute(scepOrder, CsrAttribute.ATTRIBUTE_TYPED_SAN, "rfc822:" + sanValue, true);
            } else if (GeneralName.uniformResourceIdentifier == gName.getTagNo()) {
                scepOrderUtil.setOrderAttribute(scepOrder, CsrAttribute.ATTRIBUTE_TYPED_SAN, "URI:" + sanValue, true);
            } else if (GeneralName.x400Address == gName.getTagNo()) {
                scepOrderUtil.setOrderAttribute(scepOrder, CsrAttribute.ATTRIBUTE_TYPED_SAN, "X400:" + sanValue, true);
            } else if (GeneralName.directoryName == gName.getTagNo()) {
                scepOrderUtil.setOrderAttribute(scepOrder, CsrAttribute.ATTRIBUTE_TYPED_SAN, "DirName:" + sanValue, true);
            }else {
                LOGGER.info("unexpected name / tag '{}' in SANs", gName.getTagNo());
            }
        }
    }
}
