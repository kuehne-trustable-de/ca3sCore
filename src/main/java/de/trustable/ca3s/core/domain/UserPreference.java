package de.trustable.ca3s.core.domain;


import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * A UserPreference.
 */
@Entity
@Table(name = "user_preference")
@NamedQueries({
	@NamedQuery(name = "UserPreference.findByUserId",
	query = "SELECT up FROM UserPreference up WHERE " +
			"up.userId = :userId"
    ),
    @NamedQuery(name = "UserPreference.findByNameforUser",
        query = "SELECT up FROM UserPreference up WHERE " +
            "up.name = :name and " +
            "up.userId = :userId"
    ),
    @NamedQuery(name = "UserPreference.findByNameContent",
        query = "SELECT up FROM UserPreference up WHERE " +
            "up.name = :name and " +
            "up.content = :content"
    )
})
public class UserPreference implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String USER_PREFERENCE_KEYCLOAK_ID = "KEYCLOAK_ID";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;


    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public UserPreference userId(Long userId) {
        this.userId = userId;
        return this;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public UserPreference name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public UserPreference content(String content) {
        this.content = content;
        return this;
    }

    public void setContent(String content) {
        this.content = content;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserPreference)) {
            return false;
        }
        return id != null && id.equals(((UserPreference) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "UserPreference{" +
            "id=" + getId() +
            ", userId=" + getUserId() +
            ", name='" + getName() + "'" +
            ", content='" + getContent() + "'" +
            "}";
    }
}
