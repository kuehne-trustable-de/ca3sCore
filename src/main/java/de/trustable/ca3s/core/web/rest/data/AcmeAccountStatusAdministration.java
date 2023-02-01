package de.trustable.ca3s.core.web.rest.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.trustable.ca3s.core.domain.enumeration.AccountStatus;

import java.io.Serializable;

public class AcmeAccountStatusAdministration implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 3683305069060L;

	@JsonProperty("status")
	private AccountStatus status;

	@JsonProperty("comment")
	private String comment;

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
