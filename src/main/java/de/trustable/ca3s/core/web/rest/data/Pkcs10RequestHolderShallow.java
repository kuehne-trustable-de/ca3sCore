package de.trustable.ca3s.core.web.rest.data;

import java.util.Set;

import org.bouncycastle.asn1.x509.GeneralName;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import de.trustable.ca3s.core.service.util.CSRUtil;
import de.trustable.util.Pkcs10RequestHolder;

@JsonIgnoreProperties({"p10Req", "reqAttributes", "subjectRDNs", "publicSigningKey"})
public class Pkcs10RequestHolderShallow extends Pkcs10RequestHolder {

	
	public String[] getSans() {
		
		Set<GeneralName> sanSet = CSRUtil.getSANList(getReqAttributes());
		String[] sans = new String[sanSet.size()];
		int i = 0;
		for( GeneralName gn : sanSet) {
			
			
			sans[i++] = CSRUtil.getGeneralNameDescription(gn);
		}
		return sans;
	}
	
}
