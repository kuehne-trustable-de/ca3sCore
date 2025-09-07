package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.domain.AcmeAccount;
import de.trustable.ca3s.core.domain.AcmeContact;
import de.trustable.ca3s.core.service.dto.AcmeAccountView;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AcmeAccountUtil {

    public AcmeAccountView from(AcmeAccount acmeAccount){

        AcmeAccountView acmeAccountView = new AcmeAccountView();

        acmeAccountView.setId(acmeAccount.getId());
        acmeAccountView.setAccountId(acmeAccount.getAccountId());
        acmeAccountView.setRealm(acmeAccount.getRealm());
        acmeAccountView.setStatus(acmeAccount.getStatus());
        acmeAccountView.setTermsOfServiceAgreed(acmeAccount.isTermsOfServiceAgreed());
        acmeAccountView.setCreatedOn(acmeAccount.getCreatedOn());
        acmeAccountView.setPublicKeyHash(acmeAccount.getPublicKeyHash());
        acmeAccountView.setPublicKey(acmeAccount.getPublicKey());
/*
        acmeAccountView.setKeyAlgorithm();
        int keyLength = CertificateUtil.getAlignedKeyLength(acmeAccount.getPublicKey());
        acmeAccountView.setKeyLength();
        acmeAccountView.setAltKeyAlgorithm();
*/

        List<String> contactList = new ArrayList<>();
        for(AcmeContact acmeContact: acmeAccount.getContacts()){
            contactList.add(acmeContact.getContactUrl());
        }
        acmeAccountView.setContactUrls(contactList.toArray(new String[0]));

        acmeAccountView.setEabKid(acmeAccount.getEabKid());
        acmeAccountView.setEabUser(acmeAccount.getEabUser());

        return acmeAccountView;
    }

}
