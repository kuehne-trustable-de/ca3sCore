package de.trustable.ca3s.core.service.dto.bpmn;

import com.nimbusds.jose.JWSObject;
import de.trustable.ca3s.core.service.dto.acme.AccountRequest;

import java.util.Map;

public class AcmeAccountAuthorizationInput extends BpmnInput {

    final private AccountRequest accountRequest;
    final private boolean eabPresent;
    final private boolean eabJwsNeedsValidation;
    final private String jws;

    public AcmeAccountAuthorizationInput(AccountRequest accountRequest, JWSObject jwsObject, boolean eabPresent, boolean eabJwsNeedsValidation) {
        super("ACMEAccountAuthorization");
        this.accountRequest = accountRequest;
        this.eabPresent = eabPresent;
        this.eabJwsNeedsValidation = eabJwsNeedsValidation;
        if (this.eabPresent && (jwsObject != null)) {
            this.jws = jwsObject.getParsedString();
        }else{
            this.jws = "";
        }
    }

    protected void addVariables(Map<String, Object> variables) {
        variables.put("accountRequest", accountRequest);
        variables.put("eabPresent", eabPresent);
        variables.put("eabJwsNeedsValidation", eabJwsNeedsValidation);
        variables.put("jws", jws);
    }

    public AccountRequest getAccountRequest() {
        return accountRequest;
    }

    public String getJws() {
        return jws;
    }

    public boolean isEabPresent() {
        return eabPresent;
    }

    public boolean isEabJwsNeedsValidation() {
        return eabJwsNeedsValidation;
    }
}
