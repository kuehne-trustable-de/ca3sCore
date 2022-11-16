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

import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.trustable.ca3s.core.domain.AcmeChallenge;
import de.trustable.ca3s.core.domain.enumeration.ChallengeStatus;
import de.trustable.ca3s.core.service.util.DateUtil;


/*
 *
 *
     "challenges": [
       {
         "url": "https://example.com/acme/chall/prV_B7yEyA4",
         "type": "http-01",
         "status": "valid",
         "token": "DGyRejmCefe7v4NfDGDKfA",
         "validated": "2014-12-01T12:05:58.16Z"
       }
     ],
 *
 */
@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChallengeResponse {

	@JsonProperty("url")
    private String url = null;

	@JsonProperty("type")
    private String type = null;

	@JsonProperty("status")
	private ChallengeStatus status;

	@JsonProperty("token")
    private String token = null;

	@JsonProperty("validated")
	@JsonFormat (shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss'Z'")
    private Date validated = null;

	public ChallengeResponse() {}

	public ChallengeResponse(final AcmeChallenge challengeDao, final String url) {
		this.setStatus(challengeDao.getStatus());
		this.setToken(challengeDao.getToken());
		this.setType(challengeDao.getType());
		this.setUrl(url);
		if( challengeDao.getValidated() == null) {
			this.setValidated(null);
		}else {
			this.setValidated( DateUtil.asDate(challengeDao.getValidated()));
		}
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the status
	 */
	public ChallengeStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(ChallengeStatus status) {
		this.status = status;
	}

	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * @param token the token to set
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * @return the validated
	 */
	public Date getValidated() {
		return validated;
	}

	/**
	 * @param validated the validated to set
	 */
	public void setValidated(Date validated) {
		if( validated != null) {
			this.validated = validated;
		}

	}


}

