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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/*
 * 7.1.1.  Directory


   HTTP/1.1 200 OK
   Content-Type: application/json

   {
     "newNonce": "https://example.com/acme/new-nonce",
     "newAccount": "https://example.com/acme/new-account",
     "newOrder": "https://example.com/acme/new-order",
     "newAuthz": "https://example.com/acme/new-authz",
     "revokeCert": "https://example.com/acme/revoke-cert",
     "keyChange": "https://example.com/acme/key-change",

     this inner object
     "meta": {
       "termsOfService": "https://example.com/acme/terms/2017-5-30",
       "website": "https://www.example.com/",
       "caaIdentities": ["example.com"],
       "externalAccountRequired": false
     }
   }
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class MetaInformation {

    private String termsOfService;
    private String website;
    private String[] caaIdentities;
    private boolean externalAccountRequired;

    public String getTermsOfService() {
        return termsOfService;
    }

    public void setTermsOfService(String termsOfService) {
        this.termsOfService = termsOfService;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String[] getCaaIdentities() {
        return caaIdentities;
    }

    public void setCaaIdentities(String[] caaIdentities) {
        this.caaIdentities = caaIdentities;
    }

    public boolean isExternalAccountRequired() {
        return externalAccountRequired;
    }

    public void setExternalAccountRequired(boolean externalAccountRequired) {
        this.externalAccountRequired = externalAccountRequired;
    }
}
