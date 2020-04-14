package de.trustable.ca3s.core.service.util;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.CsrAttribute;
import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.domain.RDN;
import de.trustable.ca3s.core.domain.RDNAttribute;
import de.trustable.ca3s.core.domain.RequestAttribute;
import de.trustable.ca3s.core.domain.RequestAttributeValue;
import de.trustable.ca3s.core.domain.enumeration.CsrStatus;
import de.trustable.ca3s.core.domain.enumeration.PipelineType;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CsrAttributeRepository;
import de.trustable.ca3s.core.repository.RDNAttributeRepository;
import de.trustable.ca3s.core.repository.RDNRepository;
import de.trustable.ca3s.core.repository.RequestAttributeRepository;
import de.trustable.ca3s.core.repository.RequestAttributeValueRepository;
import de.trustable.util.Pkcs10RequestHolder;

@Service
public class CSRUtil {

	private static final Logger LOG = LoggerFactory.getLogger(CSRUtil.class);

	@Autowired
	private CSRRepository csrRepository;
	
	@Autowired
	private RDNRepository rdnRepository;
	
	@Autowired
	private RequestAttributeRepository rasRepository;
	
	@Autowired
	private RequestAttributeValueRepository rasvRepository;
	
	
	@Autowired
	private RDNAttributeRepository rdnAttRepository;
	
	@Autowired
	private CsrAttributeRepository csrAttRepository;
	
	

	@Autowired
	private CryptoService cryptoUtil;

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

	public CSR buildCSR(final String csrBase64, String requestorName, final Pkcs10RequestHolder p10ReqHolder, Pipeline pipeline) {
		
		return buildCSR(csrBase64, requestorName, p10ReqHolder, pipeline.getType(), pipeline);
	}
	/**
	 * 
	 * @param csrBase64
	 * @param p10ReqHolder
	 * @param pipelineType 
	 * @return
	 */
	public CSR buildCSR(final String csrBase64, String requestorName, final Pkcs10RequestHolder p10ReqHolder, PipelineType pipelineType, Pipeline pipeline) {

		CSR csr = new CSR();

		csr.setStatus(CsrStatus.PENDING);
		
		csr.setPipeline(pipeline);
		
		csr.setPipelineType(pipelineType);
		
		csr.setCsrBase64(csrBase64);
		
		csr.setSubject(p10ReqHolder.getSubject());

		csr.setSigningAlgorithm(p10ReqHolder.getSigningAlgorithm());

		csr.setIsCSRValid(p10ReqHolder.isCSRValid());

		csr.setx509KeySpec(p10ReqHolder.getX509KeySpec());

		csr.setPublicKeyAlgorithm(p10ReqHolder.getPublicKeyAlgorithm());

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
		
		LOG.debug("RDN arr #" + p10ReqHolder.getSubjectRDNs().length);

		Set<RDN> newRdns = new HashSet<RDN>();

		for (org.bouncycastle.asn1.x500.RDN currentRdn : p10ReqHolder.getSubjectRDNs()) {

			RDN rdn = new RDN();
			rdn.csr(csr);

			LOG.debug("AttributeTypeAndValue arr #" + currentRdn.size());
			Set<RDNAttribute> rdnAttributes = new HashSet<RDNAttribute>();

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

		insertNameAttributes(csr, CsrAttribute.ATTRIBUTE_SUBJECT, p10ReqHolder.getSubjectRDNs());

		if (p10ReqHolder.getSubjectRDNs().length == 0) {

			LOG.info("Subject empty, using SANs");
			Set<GeneralName> gNameSet = getSANList(p10ReqHolder);
			for (GeneralName gName : gNameSet) {
				if (GeneralName.dNSName == gName.getTagNo()) {

					RDN rdn = new RDN();
					rdn.csr(csr);

					Set<RDNAttribute> rdnAttributes = new HashSet<RDNAttribute>();

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

		Set<RequestAttribute> newRas = new HashSet<RequestAttribute>();

		for (Attribute attr : p10ReqHolder.getReqAttributes()) {

			RequestAttribute reqAttrs = new RequestAttribute();
			reqAttrs.setCsr(csr);
			reqAttrs.setAttributeType(attr.getAttrType().toString());

			Set<RequestAttributeValue> requestAttributes = new HashSet<RequestAttributeValue>();
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
		csr.setRequestedBy(requestorName);
		csr.getCsrAttributes().add(csrAttRequestorName);
		
		rdnRepository.saveAll(csr.getRdns());
		
		for( RDN rdn: csr.getRdns()) {
			rdnAttRepository.saveAll(rdn.getRdnAttributes());
		}
		
		rasRepository.saveAll(csr.getRas());
		
		for( RequestAttribute ras: csr.getRas()) {
			rasvRepository.saveAll(ras.getRequestAttributeValues());
		}
		
		csrAttRepository.saveAll(csr.getCsrAttributes());

		csrRepository.save(csr);

		LOG.debug("saved #{} csr attributes,  ",newRas.size());

		return csr;
	}

	/**
	 * 
	 * @param p10ReqHolder
	 * @return
	 */
	Set<GeneralName> getSANList(Pkcs10RequestHolder p10ReqHolder){
		return(getSANList(p10ReqHolder.getReqAttributes() ) );
	}
	
	/**
	 * 
	 * @param reqAttributes
	 * @return
	 */
	public static Set<GeneralName> getSANList(Attribute[] reqAttributes) {
		
		Set<GeneralName> generalNameSet = new HashSet<GeneralName>();
		
		for( Attribute attr : reqAttributes) {
			if( PKCSObjectIdentifiers.pkcs_9_at_extensionRequest.equals(attr.getAttrType())){

				ASN1Set valueSet = attr.getAttrValues();
				LOG.debug("ExtensionRequest / AttrValues has {} elements", valueSet.size());
				for (ASN1Encodable asn1Enc : valueSet) {
					DERSequence derSeq = (DERSequence)asn1Enc;

					LOG.debug("ExtensionRequest / DERSequence has {} elements", derSeq.size());
					LOG.debug("ExtensionRequest / DERSequence[0] is a  {}", derSeq.getObjectAt(0).getClass().getName());

					DERSequence derSeq2 = (DERSequence)derSeq.getObjectAt(0);
					LOG.debug("ExtensionRequest / DERSequence2 has {} elements", derSeq2.size());
					LOG.debug("ExtensionRequest / DERSequence2[0] is a  {}", derSeq2.getObjectAt(0).getClass().getName());


					ASN1ObjectIdentifier objId = (ASN1ObjectIdentifier)(derSeq2.getObjectAt(0));
					if( Extension.subjectAlternativeName.equals(objId)) {
						DEROctetString derStr = (DEROctetString)derSeq2.getObjectAt(1);
						GeneralNames names = GeneralNames.getInstance(derStr.getOctets());
						LOG.debug("Attribute value SAN" + names);
						LOG.debug("SAN values #" + names.getNames().length);
						
						for (GeneralName gnSAN : names.getNames()) {
							LOG.debug("GN " + gnSAN.toString());
							generalNameSet.add(gnSAN);
							
						}
					} else {
						LOG.info("Unexpected Extensions Attribute value " + objId.getId());
					}
				}
				
			}
		}
		return generalNameSet;

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
	
	public static String getGeneralNameDescription(GeneralName gName) {
		return getGeneralNameType(gName) + " : " + gName.getName().toString();
	}
	
	public String getCSRAttribute(CSR csrDao, String name) {
		for( CsrAttribute csrAttr:csrDao.getCsrAttributes()) {
			if( csrAttr.getName().equals(name)) {
				return csrAttr.getValue();
			}
		}
		return null;
	}

	public void insertNameAttributes(CSR csr, String attributeName, org.bouncycastle.asn1.x500.RDN[] rdns) {
		for( org.bouncycastle.asn1.x500.RDN rdn: rdns ){
			for( org.bouncycastle.asn1.x500.AttributeTypeAndValue atv: rdn.getTypesAndValues()){
				String value = atv.getValue().toString().toLowerCase();
				setCsrAttribute(csr, attributeName, value, true);
				setCsrAttribute(csr, attributeName, atv.getType().getId().toLowerCase() +"="+ value, true);
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
		
		
		
		Collection<CsrAttribute> csrAttrList = csr.getCsrAttributes();
		for( CsrAttribute csrAttr : csrAttrList) {

//	        LOG.debug("checking certificate attribute '{}' containing value '{}'", certAttr.getName(), certAttr.getValue());

			if( name.equals(csrAttr.getName())) {
				if( value.equals(csrAttr.getValue())) {
					// attribute already present, no use in duplication here
					return;
				}else {
					if( !multiValue ) {
						csrAttr.setValue(value);
						return;
					}
				}
			}
		}
		
		CsrAttribute cAtt = new CsrAttribute();
		cAtt.setCsr(csr);
		cAtt.setName(name);
		cAtt.setValue(value);
		
		csr.getCsrAttributes().add(cAtt);
		
		csrAttRepository.save(cAtt);

	}



}
