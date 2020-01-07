package de.trustable.ca3s.core.security.provider;

import org.springframework.stereotype.Component;

import de.trustable.ca3s.cert.bundle.TimedRenewalCertMap;

@Component
public class TimedRenewalCertMapHolder {
    
	static TimedRenewalCertMap certMap;

	public TimedRenewalCertMap getCertMap() {
		return certMap;
	}

	public void setCertMap(TimedRenewalCertMap certMap) {
		TimedRenewalCertMapHolder.certMap = certMap;
	}
	
	
}