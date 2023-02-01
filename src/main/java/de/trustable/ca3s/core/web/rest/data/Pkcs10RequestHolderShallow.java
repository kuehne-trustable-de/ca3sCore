package de.trustable.ca3s.core.web.rest.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.puppetlabs.ssl_utils.ExtensionsUtils;
import de.trustable.ca3s.core.service.util.CSRUtil;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.util.AlgorithmInfo;
import de.trustable.util.OidNameMapper;
import de.trustable.util.Pkcs10RequestHolder;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.GeneralName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Pkcs10RequestHolderShallow {

    private final Logger LOG = LoggerFactory.getLogger(Pkcs10RequestHolderShallow.class);


    @JsonProperty("signingAlgorithmName")
	private String signingAlgorithmName;

	@JsonProperty("isCSRValid")
	private boolean isCSRValid;

	@JsonProperty("x509KeySpec")
	private String x509KeySpec;

    @JsonProperty("hashAlgName")
    private String hashAlgName;

    @JsonProperty("sigAlgName")
    private String sigAlgName;

    @JsonProperty("keyAlgName")
    private String keyAlgName;

    @JsonProperty("paddingAlgName")
    private String paddingAlgName;

    @JsonProperty("mfgName")
    private String mfgName;

    @JsonProperty("keyLength")
    private int keyLength;

    @JsonProperty("sans")
	private String[] sans;

	@JsonProperty("subject")
	private String subject;

//	private Attribute[] reqAttributes;

    @JsonProperty("csrExtensionRequests")
	private CsrReqAttribute[] csrExtensionRequests;

    @JsonProperty("publicKeyAlgorithmName")
	private String publicKeyAlgorithmName;

	public Pkcs10RequestHolderShallow( Pkcs10RequestHolder p10ReqHolder) {

		isCSRValid = p10ReqHolder.isCSRValid();
		x509KeySpec = p10ReqHolder.getX509KeySpec();
		subject = p10ReqHolder.getSubject();
		publicKeyAlgorithmName = p10ReqHolder.getPublicKeyAlgorithm();

        this.sigAlgName = OidNameMapper.lookupOid(p10ReqHolder.getSigningAlgorithm());

        AlgorithmInfo algorithmInfo = p10ReqHolder.getAlgorithmInfo();
        this.signingAlgorithmName = OidNameMapper.lookupOid(algorithmInfo.getSigAlgName());
        this.hashAlgName = OidNameMapper.lookupOid(algorithmInfo.getHashAlgName());
        this.keyAlgName = OidNameMapper.lookupOid(algorithmInfo.getSigAlgName());
        this.keyLength = CertificateUtil.getAlignedKeyLength(p10ReqHolder.getPublicSigningKey());
        this.paddingAlgName = algorithmInfo.getPaddingAlgName();
        if( "pss".equalsIgnoreCase(this.paddingAlgName )){
            this.mfgName = OidNameMapper.lookupOid(algorithmInfo.getMfgName());
        }else{
            this.mfgName = null;
        }

		Set<GeneralName> sanSet = CSRUtil.getSANList(p10ReqHolder.getReqAttributes());
		this.sans = new String[sanSet.size()];
		int i = 0;
		for( GeneralName gn : sanSet) {
			this.sans[i++] = CSRUtil.getGeneralNameDescription(gn);
		}

        List<CsrReqAttribute> csrReqAttributeList = new ArrayList<>();

        try {
            List<Map<String, Object>> extList = ExtensionsUtils.getExtensionList(p10ReqHolder.getP10Req());
            for (Map<String, Object> extMap : extList) {
                csrReqAttributeList.add(new CsrReqAttribute(extMap));
            }

        } catch (IOException e) {
            LOG.info("problem parsing CSR extensions", e);
        }
/*
        Attribute[] attributes = p10ReqHolder.getP10Req().getAttributes(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest);
        for (Attribute attribute : attributes) {
            for (ASN1Encodable value : attribute.getAttributeValues()) {
                Extensions extensions = Extensions.getInstance(value);
                for (ASN1ObjectIdentifier asn1ObjectIdentifier : extensions.getExtensionOIDs()) {

                    csrReqAttributeList.add(new CsrReqAttribute(extensions.getExtension(asn1ObjectIdentifier)));
                }
            }
        }
*/
        this.setCsrExtensionRequests(csrReqAttributeList.toArray(new CsrReqAttribute[0]));
	}

	public String getSigningAlgorithmName() {
		return signingAlgorithmName;
	}

	public boolean isCSRValid() {
		return isCSRValid;
	}

	public String getX509KeySpec() {
		return x509KeySpec;
	}

	public String[] getSans() {
		return sans;
	}

	public String getSubject() {
		return subject;
	}

	public String getPublicKeyAlgorithmName() {
		return publicKeyAlgorithmName;
	}

    public String getSigAlgName() {
        return sigAlgName;
    }

    public String getKeyAlgName() {
        return keyAlgName;
    }

    public int getKeyLength() {
        return keyLength;
    }

    public String getHashAlgName() {
        return hashAlgName;
    }

    public String getPaddingAlgName() {
        return paddingAlgName;
    }

    public String getMfgName() {
        return mfgName;
    }

    public CsrReqAttribute[] getCsrExtensionRequests() {
        return csrExtensionRequests;
    }

    public void setCsrExtensionRequests(CsrReqAttribute[] csrExtensionRequests) {
        this.csrExtensionRequests = csrExtensionRequests;
    }
}
