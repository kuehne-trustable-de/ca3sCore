package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.trustable.ca3s.core.domain.enumeration.ChallengeStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A AcmeChallenge.
 */
@Entity
@Table(name = "acme_challenge")
@NamedQueries({
    @NamedQuery(name = "AcmeChallenge.findByChallengeId",
        query = "SELECT c FROM AcmeChallenge c " +
            "WHERE " +
            "c.challengeId = :challengeId"),
    @NamedQuery(name = "AcmeChallenge.findPendingByRealm",
        query = "SELECT c FROM AcmeChallenge c " +
            "WHERE " +
            "c.status = 'PENDING' and " +
            "c.acmeAuthorization.order.status = 'PENDING' and " +
            "c.acmeAuthorization.order.realm = :realm"),
    @NamedQuery(name = "AcmeChallenge.findPendingByRequestProxy",
        query = "SELECT c FROM AcmeChallenge c " +
            "WHERE " +
            "c.status = 'PENDING' and " +
            "c.acmeAuthorization.order.status = 'PENDING' and " +
            "c.requestProxy.id  = :requestProxyId"),
})
public class AcmeChallenge implements Serializable {


    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "challenge_id", nullable = false)
    private Long challengeId;

    @NotNull
    @Column(name = "type", nullable = false)
    private String type;

    @NotNull
    @Column(name = "value_", nullable = false)
    private String value;

    @NotNull
    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "validated")
    private Instant validated;

    @Column(name = "last_error")
    private String lastError;


    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ChallengeStatus status;

    @ManyToOne
    @JsonIgnoreProperties(value = { "secret" }, allowSetters = true)
    private RequestProxyConfig requestProxy;

    @ManyToOne
    @JsonIgnoreProperties(value = { "challenges", "order" }, allowSetters = true)
    private AcmeAuthorization acmeAuthorization;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public AcmeChallenge id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChallengeId() {
        return this.challengeId;
    }

    public AcmeChallenge challengeId(Long challengeId) {
        this.setChallengeId(challengeId);
        return this;
    }

    public void setChallengeId(Long challengeId) {
        this.challengeId = challengeId;
    }

    public String getType() {
        return this.type;
    }

    public AcmeChallenge type(String type) {
        this.setType(type);
        return this;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public AcmeChallenge value(String value) {
        this.value = value;
        return this;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getToken() {
        return this.token;
    }

    public AcmeChallenge token(String token) {
        this.setToken(token);
        return this;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getValidated() {
        return this.validated;
    }

    public AcmeChallenge validated(Instant validated) {
        this.setValidated(validated);
        return this;
    }

    public void setValidated(Instant validated) {
        this.validated = validated;
    }

    public ChallengeStatus getStatus() {
        return this.status;
    }

    public AcmeChallenge status(ChallengeStatus status) {
        this.setStatus(status);
        return this;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public void setStatus(ChallengeStatus status) {
        this.status = status;
    }

    public AcmeAuthorization getAcmeAuthorization() {
        return this.acmeAuthorization;
    }

    public void setAcmeAuthorization(AcmeAuthorization acmeAuthorization) {
        this.acmeAuthorization = acmeAuthorization;
    }

    public AcmeChallenge acmeAuthorization(AcmeAuthorization acmeAuthorization) {
        this.setAcmeAuthorization(acmeAuthorization);
        return this;
    }

    public RequestProxyConfig getRequestProxy() {
        return requestProxy;
    }

    public void setRequestProxy(RequestProxyConfig requestProxy) {
        this.requestProxy = requestProxy;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AcmeChallenge)) {
            return false;
        }
        return id != null && id.equals(((AcmeChallenge) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AcmeChallenge{" +
            "id=" + getId() +
            ", challengeId=" + getChallengeId() +
            ", type='" + getType() + "'" +
            ", value='" + getValue() + "'" +
            ", token='" + getToken() + "'" +
            ", validated='" + getValidated() + "'" +
            ", RequestProxy='" + getRequestProxy() + "'" +
            ", status='" + getStatus() + "'" +
            "}";
    }

    public static final String CHALLENGE_TYPE_HTTP_01 = "http-01";
    public static final String CHALLENGE_TYPE_DNS_01 = "dns-01";
    public static final String CHALLENGE_TYPE_ALPN_01 = "tls-alpn-01";

}
