package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.service.dto.ACMEAccountView;
import de.trustable.ca3s.core.service.dto.ACMEChallengeView;
import de.trustable.ca3s.core.service.dto.ACMEOrderView;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ACMEOrderUtil {


    public ACMEOrderView from(AcmeOrder acmeOrder){

        ACMEOrderView acmeOrderView = new ACMEOrderView();

        acmeOrderView.setId(acmeOrder.getId());
        acmeOrderView.setOrderId(acmeOrder.getOrderId());
        acmeOrderView.setAccountId(acmeOrder.getAccount().getId());
        acmeOrderView.setRealm(acmeOrder.getAccount().getRealm());

        acmeOrderView.setStatus(acmeOrder.getStatus());
        if( acmeOrder.getCertificate() != null) {
            acmeOrderView.setCertificateId(acmeOrder.getCertificate().getId());
        }
        if( acmeOrder.getCsr() != null) {
            acmeOrderView.setCsrId(acmeOrder.getCsr().getId());
        }
        acmeOrderView.setExpires(acmeOrder.getExpires());
        acmeOrderView.setNotBefore(acmeOrder.getNotBefore());
        acmeOrderView.setNotAfter(acmeOrder.getNotAfter());

        acmeOrderView.setError(acmeOrder.getError());
        acmeOrderView.setFinalizeUrl(acmeOrder.getFinalizeUrl());
        acmeOrderView.setCertificateUrl(acmeOrder.getCertificateUrl());

        List<ACMEChallengeView> acmeChallengeViewList = new ArrayList<>();
        for(AcmeAuthorization acmeAuthorization: acmeOrder.getAcmeAuthorizations()){

            for(AcmeChallenge acmeChallenge: acmeAuthorization.getChallenges()){
                ACMEChallengeView acmeChallengeView = new ACMEChallengeView();
                acmeChallengeView.setAuthorizationType(acmeAuthorization.getType());
                acmeChallengeView.setAuthorizationValue(acmeAuthorization.getValue());

                acmeChallengeView.setChallengeId(acmeChallenge.getId());
                acmeChallengeView.setStatus(acmeChallenge.getStatus());
                acmeChallengeView.setType(acmeChallenge.getType());
                acmeChallengeView.setValue(acmeChallenge.getValue());
                acmeChallengeView.setValidated(acmeChallenge.getValidated());

                acmeChallengeViewList.add(acmeChallengeView);
            }
        }
        acmeOrderView.setChallenges(acmeChallengeViewList.toArray(new ACMEChallengeView[0]));
        return acmeOrderView;
    }

}
