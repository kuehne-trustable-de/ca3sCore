package de.trustable.ca3sjh.domain;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;

/**
 * A SelectorToTemplate.
 */
@Entity
@Table(name = "selector_to_template")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class SelectorToTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "selector", nullable = false)
    private String selector;

    @NotNull
    @Column(name = "template", nullable = false)
    private String template;

    @Column(name = "comment")
    private String comment;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSelector() {
        return selector;
    }

    public SelectorToTemplate selector(String selector) {
        this.selector = selector;
        return this;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getTemplate() {
        return template;
    }

    public SelectorToTemplate template(String template) {
        this.template = template;
        return this;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getComment() {
        return comment;
    }

    public SelectorToTemplate comment(String comment) {
        this.comment = comment;
        return this;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SelectorToTemplate)) {
            return false;
        }
        return id != null && id.equals(((SelectorToTemplate) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "SelectorToTemplate{" +
            "id=" + getId() +
            ", selector='" + getSelector() + "'" +
            ", template='" + getTemplate() + "'" +
            ", comment='" + getComment() + "'" +
            "}";
    }
}
