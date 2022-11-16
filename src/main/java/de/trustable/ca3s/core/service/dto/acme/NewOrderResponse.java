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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.trustable.ca3s.core.domain.AcmeIdentifier;
import de.trustable.ca3s.core.domain.AcmeOrder;
import de.trustable.ca3s.core.domain.enumeration.AcmeOrderStatus;
import de.trustable.ca3s.core.service.util.DateUtil;


/*
 *
 * {
     "status": "valid",
     "expires": "2015-03-01T14:09:07.99Z",

   "notBefore": "2016-01-01T00:00:00Z",
   "notAfter": "2016-01-08T00:00:00Z",


     "identifier": {
       "type": "dns",
       "value": "example.org"
     },

	   "authorizations": [
	     "https://example.com/acme/authz/PAniVnsZcis",
	   ],

	   "finalize": "https://example.com/acme/order/TOlocE8rfgo/finalize"
   }

 *
 */
@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewOrderResponse {

	@JsonProperty("status")
	private AcmeOrderStatus status;

	@JsonProperty("expires")
	@JsonFormat (shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss'Z'")
    private Date expires;

	@JsonProperty("identifiers")
    private Set<IdentifierResponse> identifiers;

	@JsonProperty("authorizations")
    private Set<String> authorizations;

	@JsonProperty("finalize")
	private String finalize;

	public NewOrderResponse() {}

	public NewOrderResponse( AcmeOrder orderDao, Set<String> authUrlSet, String finalizeUrl) {
		this.setStatus(orderDao.getStatus());
		this.setExpires(DateUtil.asDate(orderDao.getExpires()));

		Set<IdentifierResponse> identifiersResp = new HashSet<>();
		for( AcmeIdentifier ident: orderDao.getAcmeIdentifiers()) {
			identifiersResp.add(new IdentifierResponse(ident.getType(), ident.getValue()));
		}
		this.setIdentifiers(identifiersResp );
		this.setAuthorizations(authUrlSet);
		this.setFinalize(finalizeUrl);
	}

	/**
	 * @return the status
	 */
	public AcmeOrderStatus getStatus() {
		return status;
	}

	/**
	 * @param acmeOrderStatus the status to set
	 */
	public void setStatus(AcmeOrderStatus acmeOrderStatus) {
		this.status = acmeOrderStatus;
	}

	/**
	 * @return the expires
	 */
	@JsonFormat (shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss'Z'")
	public Date getExpires() {
		return expires;
	}

	/**
	 * @param expires the expires to set
	 */
	public void setExpires(Date expires) {
		if( expires != null) {
		  this.expires = expires;
		}
	}


	/**
	 * @return the identifiers
	 */
	public Set<IdentifierResponse> getIdentifiers() {
		return identifiers;
	}

	/**
	 * @param identifiers the identifiers to set
	 */
	public void setIdentifiers(Set<IdentifierResponse> identifiers) {
		this.identifiers = identifiers;
	}

	/**
	 * @return the authorizations
	 */
	public Set<String> getAuthorizations() {
		return authorizations;
	}

	/**
	 * @param authorizations the authorizations to set
	 */
	public void setAuthorizations(Set<String> authorizations) {
		this.authorizations = authorizations;
	}

	/**
	 * @return the finalize
	 */
	public String getFinalize() {
		return finalize;
	}

	/**
	 * @param finalize the finalize to set
	 */
	public void setFinalize(String finalize) {
		this.finalize = finalize;
	}



  }

