package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.trustable.ca3s.core.config.Constants;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.BatchSize;

/**
 * A user.
 */
@Entity
@Table(name = "jhi_user")
@NamedQueries({
    @NamedQuery(name = "User.findActiveByRole",
        query = "SELECT distinct u FROM User u JOIN u.authorities auth  WHERE " +
            " auth.name = :role AND" +
            " u.activated = TRUE"
    )
})
public class User extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 1, max = 50)
    @Column(length = 50, unique = true, nullable = false)
    private String login;

    @JsonIgnore
    @NotNull
    @Size(min = 60, max = 60)
    @Column(name = "password_hash", length = 60, nullable = false)
    private String password;

    @Size(max = 50)
    @Column(name = "first_name", length = 50)
    private String firstName;

    @Size(max = 50)
    @Column(name = "last_name", length = 50)
    private String lastName;

    @Email
    @Size(min = 5, max = 254)
    @Column(length = 254, unique = true)
    private String email;

    @Size(min = 0, max = 254)
    @Column(length = 254)
    private String phone;

    @NotNull
    @Column(name="second_factor_required", nullable = false)
    private boolean secondFactorRequired;

    @NotNull
    @Column(nullable = false)
    private boolean activated = false;

    @Size(min = 2, max = 10)
    @Column(name = "lang_key", length = 10)
    private String langKey;

    @Size(max = 256)
    @Column(name = "image_url", length = 256)
    private String imageUrl;

    @Size(max = 20)
    @Column(name = "activation_key", length = 20)
    @JsonIgnore
    private String activationKey;

    @Size(max = 20)
    @Column(name = "reset_key", length = 20)
    @JsonIgnore
    private String resetKey;

    @Column(name = "reset_date")
    private Instant resetDate = null;

    @Column(name = "failed_logins")
    private Long failedLogins;

    @Column(name = "last_login_date")
    private Instant lastloginDate = null;

    @Column(name = "blocked_until_date")
    private Instant blockedUntilDate = null;

    @Column(name = "credentials_valid_to_date")
    private Instant credentialsValidToDate = null;

    @NotNull
    @Column(name = "managed_externally", nullable = false)
    private boolean managedExternally = false;

    @Column(name = "last_user_details_update", nullable = true)
    private Instant lastUserDetailsUpdate = null;


    @ManyToOne(fetch = FetchType.EAGER)
    private Tenant tenant;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "jhi_user_authority",
        joinColumns = { @JoinColumn(name = "user_id", referencedColumnName = "id") },
        inverseJoinColumns = { @JoinColumn(name = "authority_name", referencedColumnName = "name") }
    )
    @BatchSize(size = 20)
    private Set<Authority> authorities = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    // Lowercase the login before saving it in database
    public void setLogin(String login) {
        this.login = StringUtils.lowerCase(login, Locale.ENGLISH);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {

        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isSecondFactorRequired() {
        return secondFactorRequired;
    }

    public void setSecondFactorRequired(boolean secondFactorRequired) {
        this.secondFactorRequired = secondFactorRequired;
    }

    public boolean getActivated() {
        return activated;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getActivationKey() {
        return activationKey;
    }

    public void setActivationKey(String activationKey) {
        this.activationKey = activationKey;
    }

    public String getResetKey() {
        return resetKey;
    }

    public void setResetKey(String resetKey) {
        this.resetKey = resetKey;
    }

    public Instant getResetDate() {
        return resetDate;
    }

    public void setResetDate(Instant resetDate) {
        this.resetDate = resetDate;
    }

    public String getLangKey() {
        return langKey;
    }

    public void setLangKey(String langKey) {
        this.langKey = langKey;
    }

    public Set<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<Authority> authorities) {
        this.authorities = authorities;
    }

    public Long getFailedLogins() {
        return failedLogins;
    }

    public void setFailedLogins(Long failedLogins) {
        this.failedLogins = failedLogins;
    }

    public Instant getLastloginDate() {
        return lastloginDate;
    }

    public void setLastloginDate(Instant lastloginDate) {
        this.lastloginDate = lastloginDate;
    }

    public Instant getBlockedUntilDate() {
        return blockedUntilDate;
    }

    public void setBlockedUntilDate(Instant blockedUntilDate) {
        this.blockedUntilDate = blockedUntilDate;
    }

    public Instant getCredentialsValidToDate() {
        return credentialsValidToDate;
    }

    public void setCredentialsValidToDate(Instant credentialsValidToDate) {
        this.credentialsValidToDate = credentialsValidToDate;
    }

    public boolean isManagedExternally() {
        return managedExternally;
    }

    public void setManagedExternally(boolean managedExternally) {
        this.managedExternally = managedExternally;
    }

    public Instant getLastUserDetailsUpdate() {
        return lastUserDetailsUpdate;
    }

    public void setLastUserDetailsUpdate(Instant lastUserDetailsUpdate) {
        this.lastUserDetailsUpdate = lastUserDetailsUpdate;
    }


    public Tenant getTenant() {
        return this.tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public User tenant(Tenant tenant) {
        this.setTenant(tenant);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }
        return id != null && id.equals(((User) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "User{" +
            "login='" + login + '\'' +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", email='" + email + '\'' +
            ", phone='" + phone + '\'' +
            ", imageUrl='" + imageUrl + '\'' +
            ", activated='" + activated + '\'' +
            ", langKey='" + langKey + '\'' +
            ", activationKey='" + activationKey + '\'' +
            ", managedExternally='" + managedExternally + '\'' +
            ", lastUserDetailsUpdate='" + lastUserDetailsUpdate + '\'' +
            "}";
    }
}
