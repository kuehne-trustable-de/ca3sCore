package de.trustable.ca3s.core.service.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.trustable.ca3s.core.domain.Certificate;

public class CertificateNameId  implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 6574877218033808694L;

	@JsonProperty("id")
	private Long id;

	@JsonProperty("name")
	private String name;

	public CertificateNameId() {}

	public CertificateNameId(Certificate cert) {
		this.id = cert.getId();
		this.name = cert.getSubject();
	}

}
