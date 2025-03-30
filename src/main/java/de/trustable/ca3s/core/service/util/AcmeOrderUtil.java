package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.domain.AcmeAuthorization;
import de.trustable.ca3s.core.domain.AcmeChallenge;
import de.trustable.ca3s.core.domain.AcmeOrder;
import de.trustable.ca3s.core.domain.enumeration.AcmeOrderStatus;
import de.trustable.ca3s.core.domain.enumeration.ChallengeStatus;
import de.trustable.ca3s.core.repository.AcmeOrderRepository;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.dto.AcmeOrderView;
import de.trustable.ca3s.core.service.dto.AcmeChallengeView;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

@Service
public class AcmeOrderUtil {

    static Logger LOG = LoggerFactory.getLogger(AcmeOrderUtil.class);

    private final AuditService auditService;

    private final AcmeOrderRepository orderRepository;


    public AcmeOrderUtil(AuditService auditService, AcmeOrderRepository orderRepository) {
        this.auditService = auditService;
        this.orderRepository = orderRepository;
    }

    public AcmeOrderView from(AcmeOrder acmeOrder){

        AcmeOrderView acmeOrderView = new AcmeOrderView();

        acmeOrderView.setId(acmeOrder.getId());
        acmeOrderView.setOrderId(String.valueOf(acmeOrder.getOrderId()));
        acmeOrderView.setAccountId(String.valueOf(acmeOrder.getAccount().getId()));
        acmeOrderView.setRealm(acmeOrder.getAccount().getRealm());

        acmeOrderView.setStatus(acmeOrder.getStatus());
        if( acmeOrder.getCertificate() != null) {
            acmeOrderView.setCertificateId(acmeOrder.getCertificate().getId());
        }
        if( acmeOrder.getCsr() != null) {
            acmeOrderView.setCsrId(acmeOrder.getCsr().getId());
        }
        acmeOrderView.setCreatedOn(acmeOrder.getCreatedOn());
        acmeOrderView.setExpires(acmeOrder.getExpires());
        LOG.debug("expires from AcmeOrder: " + acmeOrder.getExpires());

        acmeOrderView.setNotBefore(acmeOrder.getNotBefore());
        acmeOrderView.setNotAfter(acmeOrder.getNotAfter());

        acmeOrderView.setError(acmeOrder.getError());
        acmeOrderView.setFinalizeUrl(acmeOrder.getFinalizeUrl());
        acmeOrderView.setCertificateUrl(acmeOrder.getCertificateUrl());

        HashSet<String> urlSet = new HashSet<>();
        HashSet<String> typeSet = new HashSet<>();
        for(AcmeAuthorization acmeAuthorization: acmeOrder.getAcmeAuthorizations()){

            for(AcmeChallenge acmeChallenge: acmeAuthorization.getChallenges()){
                urlSet.add(acmeChallenge.getValue());
                typeSet.add(acmeChallenge.getType());
            }
        }

        String urls = "";
        Iterator<String> itUrl = urlSet.iterator();
        while (itUrl.hasNext()) {
            if( !urls.isEmpty()){
                urls += "; ";
            }
            urls += itUrl.next();
        }
        acmeOrderView.setChallengeUrls(urls);

        String types = "";
        Iterator<String> itType = typeSet.iterator();
        while (itType.hasNext()) {
            if( !types.isEmpty()){
                types += "; ";
            }
            types += itType.next();
        }
        acmeOrderView.setChallengeTypes(types);

        return acmeOrderView;
    }


    public List<AcmeChallengeView> challengeListfrom(AcmeOrder acmeOrder) {

        List<AcmeChallengeView> acmeChallengeViewList = new ArrayList<>();
        for (AcmeAuthorization acmeAuthorization : acmeOrder.getAcmeAuthorizations()) {

            for (AcmeChallenge acmeChallenge : acmeAuthorization.getChallenges()) {
                AcmeChallengeView acmeChallengeView = getAcmeChallengeView(acmeAuthorization, acmeChallenge);
                acmeChallengeViewList.add(acmeChallengeView);
            }
        }
        return acmeChallengeViewList;
    }

    @NotNull
    private AcmeChallengeView getAcmeChallengeView(AcmeAuthorization acmeAuthorization, AcmeChallenge acmeChallenge) {
        AcmeChallengeView acmeChallengeView = new AcmeChallengeView();
        acmeChallengeView.setAuthorizationType(acmeAuthorization.getType());
        acmeChallengeView.setAuthorizationValue(acmeAuthorization.getValue());

        acmeChallengeView.setChallengeId(acmeChallenge.getId());
        acmeChallengeView.setStatus(acmeChallenge.getStatus());
        acmeChallengeView.setType(acmeChallenge.getType());
        acmeChallengeView.setValue(acmeChallenge.getValue());
        acmeChallengeView.setValidated(acmeChallenge.getValidated());
        acmeChallengeView.setLastError(acmeChallenge.getLastError());
        return acmeChallengeView;
    }

    public void alignOrderState(AcmeOrder orderDao){

        if( orderDao.getStatus().equals(AcmeOrderStatus.READY) ){
            LOG.info("order status already '{}', no re-check after challenge state change required", orderDao.getStatus() );
            return;
        }

        if( orderDao.getStatus() != AcmeOrderStatus.PENDING) {
            LOG.warn("unexpected order status '{}' (!= Pending), no re-check after challenge state change required", orderDao.getStatus() );
            return;
        }

        boolean orderReady = true;

        /*
         * check all authorizations having at least one successfully validated challenge
         */
        for (AcmeAuthorization authDao : orderDao.getAcmeAuthorizations()) {

            boolean authReady = false;
            for (AcmeChallenge challDao : authDao.getChallenges()) {
                if (challDao.getStatus() == ChallengeStatus.VALID) {
                    LOG.debug("challenge {} of type {} is valid ", challDao.getChallengeId(), challDao.getType());
                    authReady = true;
                    break;
                }
            }
            if (authReady) {
                LOG.debug("found valid challenge, authorization id {} is valid ", authDao.getAcmeAuthorizationId());
            } else {
                LOG.debug("no valid challenge, authorization id {} and order {} still pending",
                    authDao.getAcmeAuthorizationId(), orderDao.getOrderId());
                orderReady = false;
                break;
            }
        }
        if( orderReady ){
            LOG.debug("order status set to READY" );
            auditService.saveAuditTrace(
                auditService.createAuditTraceAcmeOrderSucceeded(orderDao.getAccount(), orderDao));

            orderDao.setStatus(AcmeOrderStatus.READY);
            orderRepository.save(orderDao);
        }
    }

}
