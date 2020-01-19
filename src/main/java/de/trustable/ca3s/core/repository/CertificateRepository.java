package de.trustable.ca3s.core.repository;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.trustable.ca3s.core.domain.Certificate;


/**
 * Spring Data  repository for the Certificate entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

	  @Query(name = "Certificate.findByIssuerSerial")
	  List<Certificate> findByIssuerSerial(@Param("issuer") String issuer, 
	      @Param("serial") String serial);

	  @Query(name = "Certificate.findCACertByIssuer")
	  List<Certificate> findCACertByIssuer(@Param("issuer") String issuer);
  
	  @Query(name = "Certificate.findBySearchTermNamed1")
	  Page<Certificate> findBySearchTermNamed1(Pageable pageable, @Param("name") String name, 
		      @Param("value") String value);

	  @Query(name = "Certificate.findBySearchTermNamed1")
	  List<Certificate> findBySearchTermNamed1(@Param("name") String name, 
		      @Param("value") String value);

	  @Query(name = "Certificate.findBySearchTermNamed2")
	  Page<Certificate> findBySearchTermNamed2( 
			  Pageable pageable, 
			  @Param("name1") String name1, 
		      @Param("value1") String value1,
			  @Param("name2") String name2, 
			  @Param("value2") String value2);

	  @Query(name = "Certificate.findBySearchTermNamed2")
	  List<Certificate> findBySearchTermNamed2(
			  @Param("name1") String name1, 
		      @Param("value1") String value1,
			  @Param("name2") String name2, 
			  @Param("value2") String value2);

	  
	  @Query(name = "Certificate.findByTermNamed2")
	  List<Certificate> findByTermNamed2(
			  @Param("name1") String name1, 
		      @Param("value1") String value1,
			  @Param("name2") String name2, 
			  @Param("value2") String value2);

	  
	  
	  @Query(name = "Certificate.findByAttributeValue")
	  List<Certificate> findByAttributeValue(@Param("name") String name, 
	      @Param("value") String value);

	  @Query(name = "Certificate.findByTBSDigest")
	  List<Certificate> findByTBSDigest(@Param("tbsDigest") String tbsDigest);


}
