/*^
  ===========================================================================
  ACME server
  ===========================================================================
  Copyright (C) 2017-2018 DENIC eG, 60329 Frankfurt am Main, Germany
  ===========================================================================
  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.
  ===========================================================================
*/

package de.trustable.ca3s.core.service.dto.acme;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.trustable.ca3s.core.domain.enumeration.AccountStatus;


@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountRequest implements Serializable {

    @JsonProperty("status")
    private AccountStatus status = null;

    @JsonProperty("contact")
    private final Set<String> contacts = new HashSet<>();

    @JsonProperty("termsOfServiceAgreed")
    private Boolean termsAgreed;

    @JsonProperty("onlyReturnExisting")
    private Boolean onlyReturnExisting;

    @JsonProperty("externalAccountBinding")
    private Object externalAccountBinding = null;

    @Override
    public String toString() {
        return "AccountRequest{" +
            "status=" + status +
            ", termsAgreed=" + termsAgreed +
            ", contacts=" + contacts +
            '}';
    }

    /**
     * @return the status
     */
    public AccountStatus getStatus() {
        return status;
    }


    /**
     * @param status the status to set
     */
    public void setStatus(AccountStatus status) {
        this.status = status;
    }


    /**
     * @return the termsAgreed
     */
    public Boolean isTermsAgreed() {
        return termsAgreed;
    }

    /**
     * @param termsAgreed the termsAgreed to set
     */
    public void setTermsAgreed(boolean termsAgreed) {
        this.termsAgreed = termsAgreed;
    }

    /**
     * @return the onlyReturnExisting
     */
    public Boolean isOnlyReturnExisting() {
        return onlyReturnExisting;
    }

    /**
     * @param onlyReturnExisting the onlyReturnExisting to set
     */
    public void setOnlyReturnExisting(boolean onlyReturnExisting) {
        this.onlyReturnExisting = onlyReturnExisting;
    }

    /**
     * @return the externalAccountBinding
     */
    public Object getExternalAccountBinding() {
        return externalAccountBinding;
    }

    /**
     * @param externalAccountBinding the externalAccountBinding to set
     */
    public void setExternalAccountBinding(Object externalAccountBinding) {
        this.externalAccountBinding = externalAccountBinding;
    }

    /**
     * @return the contacts
     */
    public Set<String> getContacts() {
        return contacts;
    }

}

