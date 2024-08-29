package de.trustable.ca3s.core.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.trustable.ca3s.core.domain.Certificate;

import javax.persistence.NamedQuery;


/**
 * Spring Data  repository for the Certificate entity.
 */
@SuppressWarnings("unused")
@Repository("certificateRepository")
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

    @Query(name = "Certificate.findByAttributeValueLowerThan")
    Page<Certificate> findByAttributeValueLowerThan(Pageable pageable,
                                                    @Param("name") String name,
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


    @Query(name = "Certificate.findActiveByAttributeValue")
    List<Certificate> findActiveByAttributeValue(@Param("name") String name,
                                                 @Param("value") String value);

    @Query(name = "Certificate.findByAttributeValue")
    List<Certificate> findByAttributeValue(@Param("name") String name,
                                           @Param("value") String value);

    @Query(name = "Certificate.findByTBSDigest")
    List<Certificate> findByTBSDigest(@Param("tbsDigest") String tbsDigest);

    @Query(name = "Certificate.findByValidTo")
    List<Certificate> findByValidTo(@Param("after") Instant after,
                                    @Param("before") Instant before);

    @Query(name = "Certificate.findNonRevokedByTypeAndValidTo")
    List<Certificate> findNonRevokedByTypeAndValidTo(@Param("isEndEntity") boolean isEndEntity,
                                                     @Param("after") Instant after,
                                                     @Param("before") Instant before);

    @Query(name = "Certificate.findActiveCertificatesByHashAlgo")
    List<Object[]> findActiveCertificatesByHashAlgo(@Param("now") Instant now);

    @Query(name = "Certificate.findActiveCertificatesByKeyAlgo")
    List<Object[]> findActiveCertificatesByKeyAlgo(@Param("now") Instant now);

    @Query(name = "Certificate.findActiveCertificatesByKeyLength")
    List<Object[]> findActiveCertificatesByKeyLength(@Param("now") Instant now);

    @Query(name = "Certificate.findInactiveCertificatesByValidFrom")
    Page<Certificate> findInactiveCertificatesByValidFrom(Pageable pageable, @Param("now") Instant now);

    @Query(name = "Certificate.findActiveCertificatesByValidTo")
    Page<Certificate> findActiveCertificatesByValidTo(Pageable pageable, @Param("now") Instant now);

    @Query(name = "Certificate.findActiveCertificatesBySANs")
    Page<Certificate> findActiveCertificatesBySANs(Pageable pageable, @Param("now") Instant now, @Param("sans") List<String> sans);

    @Query(name = "Certificate.findActiveCertificateOrderedByCrlURL")
    List<Object[]> findActiveCertificateOrderedByCrlURL();

    @Query(name = "Certificate.findDistinctCrlURLForActiveCertificates")
    List<String> findDistinctCrlURLForActiveCertificates();

    @Query(name = "Certificate.findActiveCertificatesByCRLUrlSerialInList")
    List<Certificate> findActiveCertificatesBySerialInList(@Param("crlUrl") String crlUrl, @Param("serialList") List<String> serialList);

    @Query(name = "Certificate.findCrlURLForActiveCertificates")
    List<String> findCrlURLForActiveCertificates();

    @Query(name = "Certificate.findActiveCertificateBySerial")
    List<Certificate> findActiveCertificateBySerial(@Param("serial") String serial);

    @Query(name = "Certificate.findTimestampNotExistForCA")
    List<Certificate> findTimestampNotExistForCA(@Param("caName") String caName, @Param("timestamp") String timestamp);

    @Query(name = "Certificate.findMaxTimestampForCA")
    String findMaxTimestampForCA(@Param("caName") String caName, @Param("timestamp") String timestamp);

    @Query(name = "Certificate.findActiveTLSCertificate")
    List<Certificate> findActiveTLSCertificate();

    @Query(name = "Certificate.findByRequestor")
    List<Certificate> findByRequestor(@Param("requestor") String requestor);

}
