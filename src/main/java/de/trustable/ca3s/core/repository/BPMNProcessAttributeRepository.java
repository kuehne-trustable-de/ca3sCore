package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.BPMNProcessAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the BPMNProcessAttribute entity.
 */
@SuppressWarnings("unused")
@Repository("bpmnProcessAttributeRepository")
public interface BPMNProcessAttributeRepository extends JpaRepository<BPMNProcessAttribute, Long> {


}
