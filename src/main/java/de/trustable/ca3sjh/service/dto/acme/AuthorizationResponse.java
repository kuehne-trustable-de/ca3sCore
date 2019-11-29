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

package de.trustable.ca3sjh.service.dto.acme;

import java.util.Date;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.trustable.ca3sjh.domain.enumeration.OrderStatus;



/*
 * 
 * {
     "status": "valid",
     "expires": "2015-03-01T14:09:07.99Z",

     "identifier": {
       "type": "dns",
       "value": "example.org"
     },

     "challenges": [
       {
         "url": "https://example.com/acme/chall/prV_B7yEyA4",
         "type": "http-01",
         "status": "valid",
         "token": "DGyRejmCefe7v4NfDGDKfA",
         "validated": "2014-12-01T12:05:58.16Z"
       }
     ],

     "wildcard": false
   }

 * 
 */
@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorizationResponse {

	@JsonProperty("status")
	private OrderStatus status;

	@JsonProperty("expires")
	@JsonFormat (shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss'Z'")	
    private Date expires;

	@JsonProperty("identifier")
    private IdentifierResponse identifier;
    
	@JsonProperty("challenges")
    private Set<ChallengeResponse> challenges;
    
	@JsonProperty("wildcard") 
	private boolean wildcard = false;

	/**
	 * @return the status
	 */
	public OrderStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	/**
	 * @return the expires
	 */
	public Date getExpires() {
		return expires;
	}

	/**
	 * @param expires the expires to set
	 */
	public void setExpires(Date expires) {
		this.expires = new Date(expires.getTime());
	}

	/**
	 * @return the identifiers
	 */
	public IdentifierResponse getIdentifier() {
		return identifier;
	}

	/**
	 * @param identifiers the identifier to set
	 */
	public void setIdentifier(IdentifierResponse identifier) {
		this.identifier = identifier;
	}

	/**
	 * @return the challenges
	 */
	public Set<ChallengeResponse> getChallenges() {
		return challenges;
	}

	/**
	 * @param challenges the challenges to set
	 */
	public void setChallenges(Set<ChallengeResponse> challenges) {
		this.challenges = challenges;
	}

	/**
	 * @return the wildcard
	 */
	public boolean isWildcard() {
		return wildcard;
	}

	/**
	 * @param wildcard the wildcard to set
	 */
	public void setWildcard(boolean wildcard) {
		this.wildcard = wildcard;
	}
	
	
  }

