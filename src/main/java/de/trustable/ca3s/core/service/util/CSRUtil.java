package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.enumeration.CsrStatus;
import de.trustable.ca3s.core.domain.enumeration.PipelineType;
import de.trustable.ca3s.core.repository.*;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.NotificationService;
import de.trustable.ca3s.core.service.badkeys.BadKeysService;
import de.trustable.ca3s.core.service.dto.ARAContentType;
import de.trustable.util.AlgorithmInfo;
import de.trustable.util.CryptoUtil;
import de.trustable.util.OidNameMapper;
import de.trustable.util.Pkcs10RequestHolder;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.*;

import static de.trustable.ca3s.core.service.util.PipelineUtil.ADDITIONAL_EMAIL_RECIPIENTS;

@Service
public class CSRUtil {

	private static final Logger LOG = LoggerFactory.getLogger(CSRUtil.class);

	private final CSRRepository csrRepository;

    private final RDNRepository rdnRepository;

    private final CSRCommentRepository csrCommentRepository;
    private final RDNAttributeRepository rdnAttRepository;
	private final CsrAttributeRepository csrAttRepository;
    private final BadKeysService badKeysService;
	private final CryptoService cryptoUtil;
    private final PipelineUtil pipelineUtil;

    private final AuditService auditService;

    public CSRUtil(CSRRepository csrRepository, RDNRepository rdnRepository, CSRCommentRepository csrCommentRepository,
                   RDNAttributeRepository rdnAttRepository, CsrAttributeRepository csrAttRepository,
                   BadKeysService badKeysService,
                   CryptoService cryptoUtil, PipelineUtil pipelineUtil, AuditService auditService) {
        this.csrRepository = csrRepository;
        this.rdnRepository = rdnRepository;
        this.csrCommentRepository = csrCommentRepository;
        this.rdnAttRepository = rdnAttRepository;
        this.csrAttRepository = csrAttRepository;
        this.badKeysService = badKeysService;
        this.cryptoUtil = cryptoUtil;
        this.pipelineUtil = pipelineUtil;
        this.auditService = auditService;
    }
/*
    public PkcsXXData validateCSR(Pkcs10RequestHolder p10ReqHolder, Pipeline pipeline,
                            NamedValues[] arAttributes) {

        Pkcs10RequestHolderShallow p10ReqHolderShallow = new Pkcs10RequestHolderShallow(p10ReqHolder);
        PkcsXXData p10ReqData = new PkcsXXData(p10ReqHolderShallow);

        if (badKeysService.isInstalled()) {
            BadKeysResult badKeysResult;
            try {
                badKeysResult = badKeysService.checkContent(CryptoUtil.pkcs10RequestToPem(p10ReqHolder.getP10Req()));
                if (!badKeysResult.isValid()) {
                    LOG.info("badKeysResult '{}'", badKeysResult.getResponse().getResults().getResultType());
                } else {
                    LOG.debug("BadKeys not installed");
                }
                p10ReqData.setBadKeysResult(badKeysResult);
            } catch (IOException e) {
                LOG.error("Converting pkcs10Request to PEM failed unexpectedly",e);
            }
        }

        List<CSR> csrList = csrRepository.findNonRejectedByPublicKeyHash(p10ReqHolder.getPublicKeyHash());
        if(!csrList.isEmpty()) {
            LOG.info("public key with hash '{}' already used in #{} CSRs.", p10ReqHolder.getPublicKeyHash(), csrList.size());
        }

        p10ReqData.setCsrPublicKeyPresentInDB(!csrList.isEmpty());
        if (csrList.isEmpty()) {

            if (pipeline != null) {
                List<String> messageList = new ArrayList<>();
                if (pipelineUtil.isPipelineRestrictionsResolved(pipeline, p10ReqHolder, arAttributes, messageList)) {
                    LOG.debug("pipeline restrictions for pipeline '{}' solved", pipeline.getName());
                } else {
                    p10ReqData.setWarnings(messageList.toArray(new String[0]));
                }
            } else {
                LOG.info("no pipeline checks performed");
            }

        }
        return p10ReqData;
    }
*/
    /**
     *
     * @param csrBytes
     * @return
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public Pkcs10RequestHolder parseCSR(final byte[] csrBytes) throws IOException, GeneralSecurityException {

        return cryptoUtil.parseCertificateRequest(Base64.getEncoder().encodeToString(csrBytes));

    }

    /**
     *
     * @param csrBase64
     * @return
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public Pkcs10RequestHolder parseBase64CSR(final String csrBase64) throws IOException, GeneralSecurityException {

        Pkcs10RequestHolder p10ReqHolder = cryptoUtil.parseCertificateRequest(csrBase64);

        // @ToDo perform some checks

        return p10ReqHolder;
    }

    public CSR buildCSR(final String csrBase64, String requestorName, final Pkcs10RequestHolder p10ReqHolder, Pipeline pipeline) throws IOException {

		return buildCSR(csrBase64, requestorName, p10ReqHolder, pipeline.getType(), pipeline);
	}
	/**
	 *
	 * @param csrBase64
	 * @param p10ReqHolder
	 * @param pipelineType
	 * @return
	 * @throws IOException
	 */
	public CSR buildCSR(final String csrBase64, String requestorName, final Pkcs10RequestHolder p10ReqHolder,
                        PipelineType pipelineType, Pipeline pipeline) throws IOException {

		CSR csr = new CSR();

		csr.setStatus(CsrStatus.PENDING);

		csr.setPipeline(pipeline);

		csr.setPipelineType(pipelineType);

		// avoid to forward the initial CSR text: don't store accidentally included private keys or XSS attacks
//		csr.setCsrBase64(csrBase64);

		csr.setCsrBase64(CryptoUtil.pkcs10RequestToPem(p10ReqHolder.getP10Req()));

		csr.setSubject(p10ReqHolder.getSubject());

		/**
		 * produce a readable form of algorithms
		 */
        String sigAlgName = OidNameMapper.lookupOid(p10ReqHolder.getSigningAlgorithm());
        String keyAlgName = OidNameMapper.lookupOid(p10ReqHolder.getPublicKeyAlgorithm());

        csr.setSigningAlgorithm(sigAlgName);

		csr.setIsCSRValid(p10ReqHolder.isCSRValid());

		csr.setx509KeySpec(p10ReqHolder.getX509KeySpec());

		csr.setPublicKeyAlgorithm(keyAlgName);

		csr.setPublicKeyHash(p10ReqHolder.getPublicKeyHash());

		csr.setKeyLength( CertificateUtil.getAlignedKeyLength(p10ReqHolder.getPublicSigningKey()));

		csr.setServersideKeyGeneration(false);

		csr.setSubjectPublicKeyInfoBase64(p10ReqHolder.getSubjectPublicKeyInfoBase64());

		/*
		 * if( p10ReqHolder.publicSigningKey != null ){ try {
		 * this.setPublicKeyPEM(cryptoUtil.publicKeyToPem(
		 * p10ReqHolder.publicSigningKey)); } catch (IOException e) {
		 * logger.warn("wrapping of public key into PEM failed."); } }
		 */

		// not yet ...
//				setProcessInstanceId(processInstanceId);
		csr.setRequestedOn(Instant.now());

		csr.setRequestedBy(requestorName);

		csrRepository.save(csr);

        AlgorithmInfo algorithmInfo = p10ReqHolder.getAlgorithmInfo();

        setCsrAttribute(csr, CsrAttribute.ATTRIBUTE_HASH_ALGO, algorithmInfo.getHashAlgName(), false);
        setCsrAttribute(csr, CsrAttribute.ATTRIBUTE_SIGN_ALGO, algorithmInfo.getSigAlgName(), false);
        setCsrAttribute(csr, CsrAttribute.ATTRIBUTE_PADDING_ALGO, algorithmInfo.getPaddingAlgName(), false);

        if( algorithmInfo.getMfgName() != null && !algorithmInfo.getMfgName().isEmpty()) {
            setCsrAttribute(csr, CsrAttribute.ATTRIBUTE_MFG, algorithmInfo.getMfgName(), false);
        }

		LOG.debug("RDN arr #" + p10ReqHolder.getSubjectRDNs().length);

		Set<RDN> newRdns = new HashSet<>();

		for (org.bouncycastle.asn1.x500.RDN currentRdn : p10ReqHolder.getSubjectRDNs()) {

			RDN rdn = new RDN();
			rdn.csr(csr);

			LOG.debug("AttributeTypeAndValue arr #" + currentRdn.size());
			Set<RDNAttribute> rdnAttributes = new HashSet<>();

			AttributeTypeAndValue[] attrTVArr = currentRdn.getTypesAndValues();
			for (AttributeTypeAndValue attrTV : attrTVArr) {
				RDNAttribute rdnAttr = new RDNAttribute();
				rdnAttr.setRdn(rdn);
				rdnAttr.setAttributeType(attrTV.getType().toString());
				rdnAttr.setAttributeValue(attrTV.getValue().toString());
				rdnAttributes.add(rdnAttr);
			}

			rdn.setRdnAttributes(rdnAttributes);
			newRdns.add(rdn);
		}


		try {
			insertNameAttributes(csr, CsrAttribute.ATTRIBUTE_SUBJECT, new LdapName(p10ReqHolder.getSubject()));
		} catch (InvalidNameException e) {
			LOG.info("problem parsing RDN for {}", p10ReqHolder.getSubject());
		}

		insertNameAttributes(csr, CsrAttribute.ATTRIBUTE_SUBJECT, p10ReqHolder.getSubjectRDNs());

		Set<GeneralName> gNameSet = getSANList(p10ReqHolder);

		String allSans = "";
		LOG.debug("putting SANs into CSRAttributes");
		for (GeneralName gName : gNameSet) {

			String sanValue = gName.getName().toString();
			if (GeneralName.otherName == gName.getTagNo()) {
				sanValue = "--other value--";
            } else if (GeneralName.iPAddress == gName.getTagNo()) {
                sanValue = CertificateUtil.getSAN(gName);
			}

			if( allSans.length() > 0) {
				allSans += ";";
			}
			allSans += sanValue;

			this.setCsrAttribute(csr, CsrAttribute.ATTRIBUTE_SAN, sanValue, true);
			if (GeneralName.dNSName == gName.getTagNo()) {
				this.setCsrAttribute(csr, CsrAttribute.ATTRIBUTE_TYPED_SAN, "DNS:" + sanValue, true);
			} else if (GeneralName.iPAddress == gName.getTagNo()) {
                this.setCsrAttribute(csr, CsrAttribute.ATTRIBUTE_TYPED_SAN, "IP:" + sanValue, true);
			} else if (GeneralName.ediPartyName == gName.getTagNo()) {
				this.setCsrAttribute(csr, CsrAttribute.ATTRIBUTE_TYPED_SAN, "EDI:" + sanValue, true);
			} else if (GeneralName.otherName == gName.getTagNo()) {
				this.setCsrAttribute(csr, CsrAttribute.ATTRIBUTE_TYPED_SAN, "other:" + sanValue, true);
			} else if (GeneralName.registeredID == gName.getTagNo()) {
				this.setCsrAttribute(csr, CsrAttribute.ATTRIBUTE_TYPED_SAN, "regID:" + sanValue, true);
			} else if (GeneralName.rfc822Name == gName.getTagNo()) {
				this.setCsrAttribute(csr, CsrAttribute.ATTRIBUTE_TYPED_SAN, "rfc822:" + sanValue, true);
			} else if (GeneralName.uniformResourceIdentifier == gName.getTagNo()) {
				this.setCsrAttribute(csr, CsrAttribute.ATTRIBUTE_TYPED_SAN, "URI:" + sanValue, true);
			} else if (GeneralName.x400Address == gName.getTagNo()) {
				this.setCsrAttribute(csr, CsrAttribute.ATTRIBUTE_TYPED_SAN, "X400:" + sanValue, true);
			} else if (GeneralName.directoryName == gName.getTagNo()) {
				this.setCsrAttribute(csr, CsrAttribute.ATTRIBUTE_TYPED_SAN, "DirName:" + sanValue, true);
			}else {
				LOG.info("unexpected name / tag '{}' in SANs", gName.getTagNo());
			}
		}
		csr.setSans(CryptoUtil.limitLength(allSans, 250));

		if (p10ReqHolder.getSubjectRDNs().length == 0) {

			LOG.info("Subject empty, using SANs");
			for (GeneralName gName : gNameSet) {
				if (GeneralName.dNSName == gName.getTagNo()) {

					RDN rdn = new RDN();
					rdn.csr(csr);

					Set<RDNAttribute> rdnAttributes = new HashSet<>();

					RDNAttribute rdnAttr = new RDNAttribute();
					rdnAttr.setRdn(rdn);
					rdnAttr.setAttributeType(X509ObjectIdentifiers.commonName.toString());
					rdnAttr.setAttributeValue(gName.getName().toString());
					rdnAttributes.add(rdnAttr);

					rdn.setRdnAttributes(rdnAttributes);
					newRdns.add(rdn);

					LOG.info("First DNS SAN inserted as CN: " + gName.getName().toString());
					break; // just one CN !
				}
			}

		}

		csr.setRdns(newRdns);

		Set<RequestAttribute> newRas = new HashSet<>();

		for (Attribute attr : p10ReqHolder.getReqAttributes()) {

			RequestAttribute reqAttrs = new RequestAttribute();
			reqAttrs.setCsr(csr);
			reqAttrs.setAttributeType(attr.getAttrType().toString());

			Set<RequestAttributeValue> requestAttributes = new HashSet<>();
			String type = attr.getAttrType().toString();
			ASN1Set valueSet = attr.getAttrValues();
			LOG.debug("AttributeSet type " + type + " #" + valueSet.size());

			for (ASN1Encodable asn1Enc : valueSet.toArray()) {
				String value = asn1Enc.toString();
				LOG.debug("Attribute value " + value);

				RequestAttributeValue reqAttrValue = new RequestAttributeValue();
				reqAttrValue.setReqAttr(reqAttrs);
				reqAttrValue.setAttributeValue(asn1Enc.toString());
				requestAttributes.add(reqAttrValue);
			}
			reqAttrs.setRequestAttributeValues(requestAttributes);
			newRas.add(reqAttrs);
		}
		csr.setRas(newRas);

		// add requestor
		CsrAttribute csrAttRequestorName = new CsrAttribute();
		csrAttRequestorName.setCsr(csr);
		csrAttRequestorName.setName(CsrAttribute.ATTRIBUTE_REQUESTED_BY);
		csrAttRequestorName.setValue(requestorName);
		csr.getCsrAttributes().add(csrAttRequestorName);

        setCSRAttributeVersion(csr, "" + CsrAttribute.CURRENT_ATTRIBUTES_VERSION);

		rdnRepository.saveAll(csr.getRdns());

		for( RDN rdn: csr.getRdns()) {
			rdnAttRepository.saveAll(rdn.getRdnAttributes());
		}
		/*
		rasRepository.saveAll(csr.getRas());

		for( RequestAttribute ras: csr.getRas()) {
			rasvRepository.saveAll(ras.getRequestAttributeValues());
		}
		*/
		csrAttRepository.saveAll(csr.getCsrAttributes());

		csrRepository.save(csr);

		LOG.debug("saved #{} csr attributes",newRas.size());

		return csr;
	}

    public void setCSRAttributeVersion(CSR csr, String version) {
        setCsrAttribute(csr, CertificateAttribute.ATTRIBUTE_ATTRIBUTES_VERSION, version, false);
    }

    public static String getKeyAlgoName(String sigAlgName) {
        String keyAlgName = sigAlgName;
        if( sigAlgName.toLowerCase().contains("with")) {
            String[] parts = sigAlgName.toLowerCase().split("with");
            if(parts.length > 1) {
                if(parts[1].contains("and")) {
                    String[] parts2 = parts[1].split("and");
                    keyAlgName = parts2[0];
                }else {
                    keyAlgName = parts[1];
                }
            }
        }
        return keyAlgName;
    }

    public String getCommonName(CSR csr) {
        for( RDN rdn : csr.getRdns()){
            for( RDNAttribute rdnAttribute :rdn.getRdnAttributes()){
                if(X509ObjectIdentifiers.commonName.toString().equals(rdnAttribute.getAttributeType())){
                    return rdnAttribute.getAttributeValue();
                }
            }
        }
        return "";
    }


    /**
	 *
	 * @param p10ReqHolder
	 * @return
	 */
	public Set<GeneralName> getSANList(Pkcs10RequestHolder p10ReqHolder){
		return(getSANList(p10ReqHolder.getReqAttributes() ) );
	}

    public void setCSRComment(CSR csr, String commentIn) {

	    String comment = (commentIn == null)?"":commentIn;

        CSRComment csrComment = (csr.getComment() == null) ? new CSRComment() : csr.getComment();
        String oldCommentText = (csrComment.getComment() == null) ? "" : csrComment.getComment();
        if (!oldCommentText.trim().equals(comment.trim())) {
            csrComment.setCsr(csr);
            csrComment.setComment(comment);
            csrCommentRepository.save(csrComment);

            auditService.saveAuditTrace(auditService.createAuditTraceCsrAttribute(AuditService.AUDIT_CSR_COMMENT_CHANGED,
                oldCommentText, comment, csr));
        }
    }

    /*
    byte[] der = Files.readAllBytes(Paths.get(args[0])); // for example
    // assuming all BouncyCastle classes imported as needed and
    // given a CSR in der, to get the SAN extension as an object
    // (minimal error handling, you may want to improve)
    Attribute[] attrs = new PKCS10CertificationRequest(der).getAttributes(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest);
    if( attrs.length != 1 ) throw new Exception("bad");
    ASN1Encodable[] valus = attrs[0].getAttributeValues();
    if( valus.length != 1 ) throw new Exception("bad");
    Extension extn = Extensions.getInstance(valus[0]).getExtension(Extension.subjectAlternativeName);
    if( extn == null ) throw new Exception("missing");
    // to get the _value_ of the extension, now extn.getExtnValus().getOctets()
    // to _use_ the _value_ of the extension, parse as GeneralNames:
    GeneralNames sanv = GeneralNames.getInstance(extn.getExtnValue().getOctets());
    for( GeneralName item : sanv.getNames() ){ // example of possible usage
        System.out.println (item.toString()); // you probably want something else
    }

     */


    public static void retrieveSANFromCSRAttribute(Set<GeneralName> sanSet, Attribute attrExtension ){

        ASN1Set valueSet = attrExtension.getAttrValues();
        LOG.info( "ExtensionRequest / AttrValues has " + valueSet.size() + " elements" );
        for (ASN1Encodable asn1Enc : valueSet) {

            if( asn1Enc instanceof DERSequence ) {
	            DERSequence derSeq = (DERSequence)asn1Enc;
	            LOG.debug( "ExtensionRequest / DERSequence has "+derSeq.size()+" elements" );
	            extractSANsFromArray(sanSet, derSeq.toArray());
            }else if( asn1Enc instanceof DLSequence ) {
            	DLSequence dlSeq = (DLSequence)asn1Enc;
	            LOG.debug( "ExtensionRequest / DLSequence has "+dlSeq.size()+" elements" );
	            extractSANsFromArray(sanSet, dlSeq.toArray());
            } else {
                LOG.info( "asn1Enc in valueSet is of an unexpected type " + asn1Enc.getClass().getName());
            }
        }

    }

    /**
     * Extract all SANs from an ASN1Encodable
     *
     * @param sanSet
     * @param asn1Array
     */
	static void extractSANsFromArray(Set<GeneralName> sanSet, ASN1Encodable[] asn1Array) {
		for( ASN1Encodable asn1Enc : asn1Array) {

			LOG.debug( "ExtensionRequest / asn1Enc2 is a " + asn1Enc.getClass().getName());

		    ASN1Encodable asn1EncValue;
		    ASN1ObjectIdentifier objId;

            if( asn1Enc instanceof DERSequence ) {
    		    DERSequence derSeq2 = (DERSequence) asn1Enc;
    		    LOG.debug( "ExtensionRequest / DERSequence2 has " + derSeq2.size() + " elements");
                LOG.debug( "ExtensionRequest / DERSequence2[0] is a " + derSeq2.getObjectAt(0).getClass().getName());
                LOG.debug( "ExtensionRequest / DERSequence2[1] is a " + derSeq2.getObjectAt(1).getClass().getName());

    		    objId = (ASN1ObjectIdentifier) (derSeq2.getObjectAt(0));
    		    asn1EncValue = derSeq2.getObjectAt(1);
                if( (derSeq2.size() > 2) && !(asn1EncValue instanceof DEROctetString)){
                    LOG.debug( "ExtensionRequest / DERSequence2[2] is a " + derSeq2.getObjectAt(2).getClass().getName());
                    asn1EncValue = derSeq2.getObjectAt(2);
                }

            }else if( asn1Enc instanceof DLSequence ) {
            	DLSequence dlSeq = (DLSequence)asn1Enc;
	            LOG.debug( "DLSequence has "+dlSeq.size()+" elements" );
                LOG.debug( "ExtensionRequest / DLSequence[0] is a " + dlSeq.getObjectAt(0).getClass().getName());
                LOG.debug( "ExtensionRequest / DLSequence[1] is a " + dlSeq.getObjectAt(1).getClass().getName());

    		    objId = (ASN1ObjectIdentifier) (dlSeq.getObjectAt(0));
    		    asn1EncValue = dlSeq.getObjectAt(1);
                if( (dlSeq.size() > 2) && !(asn1EncValue instanceof DEROctetString)){
                    LOG.debug( "ExtensionRequest / DLSequence[2] is a " + dlSeq.getObjectAt(2).getClass().getName());
                    asn1EncValue = dlSeq.getObjectAt(2);
                }

            } else {
                LOG.info( "asn1Enc in asn1Array is of an unexpected type " + asn1Enc.getClass().getName());
                continue;
            }

		    LOG.debug("ExtensionRequest / DERSequence2[1] (asn1EncValue)is a " + asn1EncValue.getClass().getName());


		    String attrReadableName = OidNameMapper.lookupOid(objId.getId());

		    if (Extension.subjectAlternativeName.equals(objId)) {
		        DEROctetString derStr = (DEROctetString) asn1EncValue;
		        byte[] valBytes = derStr.getOctets();

		        GeneralNames names = GeneralNames.getInstance(valBytes);
		        LOG.debug("Attribute value SAN" + names);
		        LOG.debug("SAN values #" + names.getNames().length);

		        for (GeneralName gnSAN : names.getNames()) {
		        	LOG.debug( "GN " + gnSAN.getName().toString());
		        	sanSet.add(gnSAN);
		        }
		    } else {
		        String stringValue = asn1EncValue.toString();

		        Method[] methods = asn1EncValue.getClass().getMethods();

		        for( Method m: methods){
//                        Log.d(TAG, "checking method " + m.getName());
		            try {

		                if( "getString".equals(m.getName())){
		                    stringValue = (String)m.invoke(asn1EncValue);
		                    break;
		                }else if( "getOctets".equals(m.getName())){
		                    stringValue = new String((byte[])m.invoke(asn1EncValue));
		                    break;
		                }else if( "getValue".equals(m.getName())){
		                    stringValue = (String)m.invoke(asn1EncValue);
		                    break;
		                }else if( "getId".equals(m.getName())){
		                    stringValue = OidNameMapper.lookupOid((String)m.invoke(asn1EncValue));
		                    break;
		                }else if( "getAdjustedDate".equals(m.getName())){
		                    stringValue = (String)m.invoke(asn1EncValue);
		                    break;
		                }
		            } catch (IllegalAccessException | InvocationTargetException e) {
		            	LOG.debug( "invoking " + m.getName(), e);
		            }
		        }
		        LOG.debug("found attrReadableName '{}' with value '{}'", attrReadableName, stringValue);

		    }
		}
	}


	/**
	 *
	 * @param reqAttributes
	 * @return
	 */
	public static Set<GeneralName> getSANList(Attribute[] reqAttributes) {

		Set<GeneralName> generalNameSet = new HashSet<>();

		for( Attribute attr : reqAttributes) {
			if( PKCSObjectIdentifiers.pkcs_9_at_extensionRequest.equals(attr.getAttrType())){

                Extensions extensions = Extensions.getInstance(attr.getAttrValues().getObjectAt(0));

                GeneralNames gns = GeneralNames.fromExtensions(extensions, Extension.subjectAlternativeName);
                if( gns != null) {

                    GeneralName[] names = gns.getNames();
                    for (GeneralName name : names) {
                        LOG.info("Type: " + name.getTagNo() + " | Name: " + name.getName());
                        generalNameSet.add(name);
                    }
                }
			}
		}
		return generalNameSet;
	}

    public boolean isCNinSANSet(Pkcs10RequestHolder p10ReqHolder) {

        Set<GeneralName> sanSet = getSANList(p10ReqHolder);

        if(Arrays.stream(p10ReqHolder.getSubjectRDNs()).noneMatch(
            rdn -> rdn.getFirst() != null && BCStyle.CN.equals(rdn.getFirst().getType())
        )) {;
            return true;
        }

        return Arrays.stream(p10ReqHolder.getSubjectRDNs()).filter(
            rdn -> rdn.getFirst() != null && BCStyle.CN.equals(rdn.getFirst().getType())
        ).anyMatch(
            rdn -> {
                String cn = rdn.getFirst().getValue().toString();
                boolean cnInSan = sanSet.stream().anyMatch(
                    gn -> {
                        String sanValue = CertificateUtil.getTypedSAN(gn);
                        return sanValue.equalsIgnoreCase("DNS:" + cn);
                    }
                );
                return cnInSan;
            }
        );
    }

    /**
 *
 * @param gName
 * @return
 */
	public static String getGeneralNameType(GeneralName gName) {
		if (GeneralName.dNSName == gName.getTagNo()) {
			return "DNS name";
		} else if (GeneralName.directoryName == gName.getTagNo()) {
			return "directory name";
		} else if (GeneralName.ediPartyName == gName.getTagNo()) {
			return "edi party name";
		} else if (GeneralName.iPAddress == gName.getTagNo()) {
			return "ip address";
		} else if (GeneralName.otherName == gName.getTagNo()) {
			return "other name";
		} else if (GeneralName.rfc822Name == gName.getTagNo()) {
			return "rfc822 name";
		} else if (GeneralName.uniformResourceIdentifier == gName.getTagNo()) {
			return "URI";
		} else if (GeneralName.x400Address == gName.getTagNo()) {
			return "x400 address";
		} else {
			return "unexpected identifier '" + gName.getTagNo() + "'";
		}
	}

    public static String getCnOrFirstSan(final CSR csr) {

        for( RDN rdn : csr.getRdns()){
            for( RDNAttribute rdnAttribute :rdn.getRdnAttributes()){
                if(X509ObjectIdentifiers.commonName.toString().equals(rdnAttribute.getAttributeType())){
                    return rdnAttribute.getAttributeValue();
                }
            }
        }

        for (CsrAttribute csrAttribute : csr.getCsrAttributes()) {

            if (CertificateAttribute.ATTRIBUTE_SAN.equals(csrAttribute.getName())) {
                return csrAttribute.getValue();
            }
        }
        return "(noCommonName)";
    }

    public static String getGeneralNameDescription(GeneralName gName) {
/*
        LOG.info("gName.getName() is a " + gName.getName().getClass().getName());
        if( gName.getName() instanceof DERIA5String){
            return CertificateUtil.getTypedSAN(gName.getTagNo(), ((DERIA5String)gName.getName()).getString());
        }
        return CertificateUtil.getTypedSAN(gName.getTagNo(), gName.getName().toString());

 */
        return CertificateUtil.getTypedSAN(gName);

	}

	/**
	 *
	 * @param csrDao
	 * @param status
	 */
    public void setStatus(CSR csrDao, CsrStatus status) {
        csrDao.setStatus(status);
        csrRepository.save(csrDao);
    }


    public void setStatusAndRejectionReason(CSR csr, CsrStatus status, String reason) {
        csr.setStatus(status);
        csr.setStatus(status);
        csr.setRejectedOn(Instant.now());
        csr.setRejectionReason(CryptoUtil.limitLength(reason, 250));
        csrRepository.save(csr);
    }


    /**
	 *
	 * @param csrDao
	 * @param name
	 * @return
	 */
	public String getCSRAttribute(CSR csrDao, String name) {
		for( CsrAttribute csrAttr:csrDao.getCsrAttributes()) {
			if( csrAttr.getName().equals(name)) {
				return csrAttr.getValue();
			}
		}
		return null;
	}



	public void insertNameAttributes(CSR csr, String attributeName, LdapName ldapName) {
		List<Rdn> rdnList = ldapName.getRdns();
		for( Rdn rdn: rdnList) {
    		String rdnExpression = rdn.getType().toLowerCase() + "=" + rdn.getValue().toString().toLowerCase().trim();
			setCsrAttribute(csr, attributeName, rdnExpression, true);
		}
	}

	public void insertNameAttributes(CSR csr, String attributeName, org.bouncycastle.asn1.x500.RDN[] rdns) {

		for( org.bouncycastle.asn1.x500.RDN rdn: rdns ){
			for( org.bouncycastle.asn1.x500.AttributeTypeAndValue atv: rdn.getTypesAndValues()){
				String value = atv.getValue().toString().toLowerCase().trim();
				setCsrAttribute(csr, attributeName, value, true);

				String oid = atv.getType().getId().toLowerCase();
				setCsrAttribute(csr, attributeName, oid +"="+ value, true);

				if( !oid.equals(atv.getType().toString().toLowerCase())) {
					setCsrAttribute(csr, attributeName, atv.getType().toString().toLowerCase() +"="+ value, true);
				}
/*
 * long text form
				String oidName = OidNameMapper.lookupOid(oid);
				if( !oid.equals(oidName.toLowerCase())) {
					setCsrAttribute(csr, attributeName, oidName +"="+ value, true);
				}
*/
			}
		}
	}

	public void setCsrAttribute(CSR csr, String name, String value, boolean multiValue) {

		if( name == null) {
			LOG.warn("no use to insert attribute with name 'null'", new Exception());
			return;
		}
		if( value == null) {
			value= "";
		}
        String limitedValue = CryptoUtil.limitLength(value, 250);

		Collection<CsrAttribute> csrAttrList = csr.getCsrAttributes();
		for( CsrAttribute csrAttr : csrAttrList) {

	        LOG.debug("checking csr attribute '{}' containing value '{}'", csrAttr.getName(), csrAttr.getValue());

			if( name.equals(csrAttr.getName())) {
				if( value.equals(csrAttr.getValue())) {
					// attribute already present, no use in duplication here
                    LOG.debug("attribute '{}' has expected value", csrAttr.getName());
					return;
				}else {
					if( !multiValue ) {
                        LOG.debug("overwriting existing attribute '{}' ", csrAttr.getName());
						csrAttr.setValue(limitedValue);
                        csrAttRepository.save(csrAttr);
						return;
					}
				}
			}
		}

		CsrAttribute cAtt = new CsrAttribute();
		cAtt.setCsr(csr);
		cAtt.setName(name);
		cAtt.setValue(limitedValue);

		csr.getCsrAttributes().add(cAtt);

		csrAttRepository.save(cAtt);
	}

    public Set<String> getAdditionalEmailRecipients(CSR csr) {
        Set<String> additionalEmailSet = new HashSet<>();
        if (csr.getPipeline() != null) {
            Pipeline pipeline = csr.getPipeline();
            String emails = pipelineUtil.getPipelineAttribute(
                pipeline,
                ADDITIONAL_EMAIL_RECIPIENTS,
                "");
            NotificationService.addSplittedEMailAddress(additionalEmailSet, emails);

            List<String> nameList = pipelineUtil.getTypedARAttributeNames(pipeline, ARAContentType.EMAIL_ADDRESS);

            for (String attributeName : nameList) {
                String email = getCSRAttribute(
                    csr,
                    CsrAttribute.ARA_PREFIX + attributeName);

                if (email != null) {
                    LOG.debug("Email ara attribute {} has value '{}' added", attributeName, email);
                    additionalEmailSet.add(email);
                }
            }
        }
        return additionalEmailSet;
    }

}
