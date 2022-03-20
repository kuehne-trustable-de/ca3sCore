package de.trustable.ca3s.core.web.rest.data;

import java.util.Set;

import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.util.AlgorithmInfo;
import de.trustable.util.OidNameMapper;
import org.bouncycastle.asn1.x509.GeneralName;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.trustable.ca3s.core.service.util.CSRUtil;
import de.trustable.util.Pkcs10RequestHolder;

public class Pkcs10RequestHolderShallow {


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
}
