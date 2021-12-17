package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.domain.ACMEAccount;
import de.trustable.ca3s.core.domain.AcmeContact;
import de.trustable.ca3s.core.service.dto.ACMEAccountView;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ACMEAccountUtil {


    public ACMEAccountView from(ACMEAccount acmeAccount){

        ACMEAccountView acmeAccountView = new ACMEAccountView();

        acmeAccountView.setId(acmeAccountView.getId());
        acmeAccountView.setAccountId(acmeAccount.getAccountId());
        acmeAccountView.setRealm(acmeAccount.getRealm());
        acmeAccountView.setStatus(acmeAccount.getStatus());
        acmeAccountView.setTermsOfServiceAgreed(acmeAccount.isTermsOfServiceAgreed());
        acmeAccountView.setCreatedOn(Instant.now());
        acmeAccountView.setPublicKeyHash(acmeAccount.getPublicKeyHash());

        List<String> contactList = new ArrayList<>();
        for(AcmeContact acmeContact: acmeAccount.getContacts()){
            contactList.add(acmeContact.getContactUrl());
        }
        acmeAccountView.setContactUrls(contactList.toArray(new String[0]));

        return acmeAccountView;
    }

}
