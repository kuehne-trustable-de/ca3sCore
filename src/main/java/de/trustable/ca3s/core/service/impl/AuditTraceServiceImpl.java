package de.trustable.ca3s.core.service.impl;

import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.repository.*;
import de.trustable.ca3s.core.service.AuditTraceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Service Implementation for managing {@link AuditTrace}.
 */
@Service
@Transactional
public class AuditTraceServiceImpl implements AuditTraceService {

    private final Logger log = LoggerFactory.getLogger(AuditTraceServiceImpl.class);

    private final AuditTraceRepository auditTraceRepository;
    private final CertificateRepository certificateRepository;
    private final CSRRepository csrRepository;
    private final PipelineRepository pipelineRepository;
    private final CAConnectorConfigRepository caConnectorConfigRepository;
    private final BPMNProcessInfoRepository bpmnProcessInfoRepository;
    private final AcmeOrderRepository acmeOrderRepository;
    private final ScepOrderRepository scepOrderRepository;

    public AuditTraceServiceImpl(AuditTraceRepository auditTraceRepository,
                                 CertificateRepository certificateRepository,
                                 CSRRepository csrRepository,
                                 PipelineRepository pipelineRepository,
                                 CAConnectorConfigRepository caConnectorConfigRepository,
                                 BPMNProcessInfoRepository bpmnProcessInfoRepository,
                                 AcmeOrderRepository acmeOrderRepository,
                                 ScepOrderRepository scepOrderRepository) {
        this.auditTraceRepository = auditTraceRepository;
        this.certificateRepository = certificateRepository;
        this.csrRepository = csrRepository;
        this.pipelineRepository = pipelineRepository;
        this.caConnectorConfigRepository = caConnectorConfigRepository;
        this.bpmnProcessInfoRepository = bpmnProcessInfoRepository;
        this.acmeOrderRepository = acmeOrderRepository;
        this.scepOrderRepository = scepOrderRepository;
    }

    @Override
    public AuditTrace save(AuditTrace auditTrace) {
        log.debug("Request to save AuditTrace : {}", auditTrace);
        return auditTraceRepository.save(auditTrace);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditTrace> findAll(Pageable pageable) {
        log.debug("Request to get all AuditTraces");
        return auditTraceRepository.findAll(pageable);
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<AuditTrace> findOne(Long id) {
        log.debug("Request to get AuditTrace : {}", id);
        return auditTraceRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete AuditTrace : {}", id);
        auditTraceRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditTrace> findBy(Pageable pageable,
                                   Long certificateId,
                                   Long csrId,
                                   Long pipelineId,
                                   Long caConnectorId,
                                   Long processInfoId,
                                   Long acmeOrderId,
                                   Long scepOrderId){

        if( (certificateId != -1 ) || (csrId != -1 )) {
            log.debug("Request to select AuditTrace by certificate id '{}' or csr id '{}'", certificateId, csrId);

            Certificate cert = null;
            CSR csr = null;
            if( certificateId!= -1) {
                Optional<Certificate> optCert = certificateRepository.findById(certificateId);
                if (optCert.isPresent()) {
                    cert = optCert.get();
                }
            }
            if( csrId!= -1) {
                Optional<CSR> optCsr = csrRepository.findById(csrId);
                if (optCsr.isPresent()) {
                    csr = optCsr.get();
                }
            }
            return auditTraceRepository.findByCsrAndCert(pageable, cert, csr);

        } else if( pipelineId != -1){
            log.debug("Request to select AuditTrace by pipeline id '{}'", pipelineId);
            Optional<Pipeline> optPipeline = pipelineRepository.findById(pipelineId);
            if(optPipeline.isPresent()){
                return auditTraceRepository.findByPipeline(pageable, optPipeline.get());
            }
        } else if( caConnectorId != -1){
            log.debug("Request to select AuditTrace by caConnector id '{}'", caConnectorId);
            Optional<CAConnectorConfig> optCaConfig = caConnectorConfigRepository.findById(caConnectorId);
            if(optCaConfig.isPresent()){
                return auditTraceRepository.findByCaConnector(pageable, optCaConfig.get());
            }
        } else if( processInfoId != -1){
            log.debug("Request to select AuditTrace by processInfo id '{}'", processInfoId);
            Optional<BPMNProcessInfo> optProcessInfo = bpmnProcessInfoRepository.findById(processInfoId);
            if(optProcessInfo.isPresent()){
                return auditTraceRepository.findByProcessInfo(pageable, optProcessInfo.get());

            }
        } else if( acmeOrderId != -1){
            log.debug("Request to select AuditTrace by acmeOrder id '{}'", acmeOrderId);
            Optional<AcmeOrder> optAcmeOrder = acmeOrderRepository.findById(acmeOrderId);
            if(optAcmeOrder.isPresent()){
                return auditTraceRepository.findByAcmeOrder(pageable, optAcmeOrder.get());
            }
        } else if( scepOrderId != -1){
            log.debug("Request to select AuditTrace by scepOrderId id '{}'", scepOrderId);
            Optional<ScepOrder> optScepOrder = scepOrderRepository.findById(scepOrderId);
            if(optScepOrder.isPresent()){
                return auditTraceRepository.findByScepOrder(pageable, optScepOrder.get());
            }
        } else{
            log.warn("Request to select AuditTrace : non-null argument required!");
        }
        return new PageImpl(new ArrayList<>());
    }

}
