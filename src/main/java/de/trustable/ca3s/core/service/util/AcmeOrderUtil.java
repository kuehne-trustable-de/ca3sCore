package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.domain.AcmeAuthorization;
import de.trustable.ca3s.core.domain.AcmeChallenge;
import de.trustable.ca3s.core.domain.AcmeOrder;
import de.trustable.ca3s.core.service.dto.ACMEChallengeView;
import de.trustable.ca3s.core.service.dto.ACMEOrderView;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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


    public List<ACMEChallengeView> challengeListfrom(AcmeOrder acmeOrder) {

        List<ACMEChallengeView> acmeChallengeViewList = new ArrayList<>();
        for (AcmeAuthorization acmeAuthorization : acmeOrder.getAcmeAuthorizations()) {

            for (AcmeChallenge acmeChallenge : acmeAuthorization.getChallenges()) {
                ACMEChallengeView acmeChallengeView = getAcmeChallengeView(acmeAuthorization, acmeChallenge);
                acmeChallengeViewList.add(acmeChallengeView);
            }
        }
        return acmeChallengeViewList;
    }

    @NotNull
    private ACMEChallengeView getAcmeChallengeView(AcmeAuthorization acmeAuthorization, AcmeChallenge acmeChallenge) {
        ACMEChallengeView acmeChallengeView = new ACMEChallengeView();
        acmeChallengeView.setAuthorizationType(acmeAuthorization.getType());
        acmeChallengeView.setAuthorizationValue(acmeAuthorization.getValue());

        acmeChallengeView.setChallengeId(acmeChallenge.getId());
        acmeChallengeView.setStatus(acmeChallenge.getStatus());
        acmeChallengeView.setType(acmeChallenge.getType());
        acmeChallengeView.setValue(acmeChallenge.getValue());
        acmeChallengeView.setValidated(acmeChallenge.getValidated());
        return acmeChallengeView;
    }
}
