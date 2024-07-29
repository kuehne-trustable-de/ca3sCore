package de.trustable.ca3s.core.service.dto;

import de.trustable.ca3s.core.config.Constants;

import de.trustable.ca3s.core.domain.Authority;
import de.trustable.ca3s.core.domain.Tenant;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.repository.TenantRepository;

import javax.persistence.Column;
import javax.validation.constraints.*;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A DTO representing a user, with his authorities.
 */
public class UserDTO {

    private Long id;

    @NotBlank
    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 1, max = 50)
    private String login;

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @Email
    @Size(min = 5, max = 254)
    private String email;

    @Size(max = 256)
    private String imageUrl;

    private boolean activated = false;

    @Size(min = 2, max = 10)
    private String langKey;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;

    private boolean isManagedExternally;

    private Set<String> authorities;

    private String tenantName;

    private Long tenantId;

    private Long failedLogins;

    private Instant blockedUntilDate = null;

    private Instant credentialsValidToDate = null;

    public UserDTO() {
        // Empty constructor needed for Jackson.
    }

    public UserDTO(User user, TenantRepository tenantRepository) {
        this(user);

        if(user.getTenant() != null){
            this.tenantId = user.getTenant().getId();

            Optional<Tenant> tenantOptional = tenantRepository.findById(this.tenantId);
            if( tenantOptional.isPresent()) {
                this.tenantName = tenantOptional.get().getLongname();
            }
        }
    }

    public UserDTO(User user) {
        this.id = user.getId();
        this.login = user.getLogin();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.activated = user.getActivated();
        this.imageUrl = user.getImageUrl();
        this.langKey = user.getLangKey();
        this.createdBy = user.getCreatedBy();
        this.createdDate = user.getCreatedDate();
        this.lastModifiedBy = user.getLastModifiedBy();
        this.lastModifiedDate = user.getLastModifiedDate();
        this.isManagedExternally = user.isManagedExternally();

        this.failedLogins = user.getFailedLogins();
        this.blockedUntilDate = user.getBlockedUntilDate();
        this.credentialsValidToDate = user.getCredentialsValidToDate();

        this.authorities = user.getAuthorities().stream()
            .map(Authority::getName)
            .collect(Collectors.toSet());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getLangKey() {
        return langKey;
    }

    public void setLangKey(String langKey) {
        this.langKey = langKey;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public boolean isManagedExternally() {
        return isManagedExternally;
    }

    public void setManagedExternally(boolean managedExternally) {
        isManagedExternally = managedExternally;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<String> authorities) {
        this.authorities = authorities;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getFailedLogins() {
        return failedLogins;
    }

    public void setFailedLogins(Long failedLogins) {
        this.failedLogins = failedLogins;
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

    @Override
    public String toString() {
        return "UserDTO{" +
            "id=" + id +
            ", login='" + login + '\'' +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", email='" + email + '\'' +
            ", imageUrl='" + imageUrl + '\'' +
            ", activated=" + activated +
            ", langKey='" + langKey + '\'' +
            ", createdBy='" + createdBy + '\'' +
            ", createdDate=" + createdDate +
            ", lastModifiedBy='" + lastModifiedBy + '\'' +
            ", lastModifiedDate=" + lastModifiedDate +
            ", isManagedExternally=" + isManagedExternally +
            ", authorities=" + authorities +
            ", tenantName='" + tenantName + '\'' +
            ", tenantId=" + tenantId +
            ", failedLogins=" + failedLogins +
            ", blockedUntilDate=" + blockedUntilDate +
            ", credentialsValidToDate=" + credentialsValidToDate +
            '}';
    }
}
