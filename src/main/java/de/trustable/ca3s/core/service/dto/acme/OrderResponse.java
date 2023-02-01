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
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.trustable.ca3s.core.domain.AcmeOrder;
import de.trustable.ca3s.core.service.util.DateUtil;


/*
 *

{
     "status": "valid",
     "expires": "2015-12-31T00:17:00.00-09:00",

     "notBefore": "2015-12-31T00:17:00.00-09:00",
     "notAfter": "2015-12-31T00:17:00.00-09:00",

     "identifiers": [
       { "type": "dns", "value": "example.com" },
       { "type": "dns", "value": "www.example.com" }
     ],

     "authorizations": [
       "https://example.com/acme/authz/PAniVnsZcis",
       "https://example.com/acme/authz/r4HqLzrSrpI"
     ],

     "finalize": "https://example.com/acme/order/TOlocE8rfgo/finalize",

     "certificate": "https://example.com/acme/cert/mAt3xBGaobw"
   }

 *
 */
@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse extends NewOrderResponse {

	@JsonProperty("notBefore")
	@JsonFormat (shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss'Z'")
    private Date notBefore;

	@JsonProperty("notAfter")
	@JsonFormat (shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss'Z'")
    private Date notAfter;

	@JsonProperty("certificate")
	private String certificate;

	public OrderResponse (AcmeOrder orderDao, Set<String> authUrlSet, String finalizeUrl, String certificateUrl) {
		super(orderDao, authUrlSet, finalizeUrl);
        if(orderDao.getNotBefore() != null) {
            this.setNotBefore(DateUtil.asDate(orderDao.getNotBefore()));
        }
        if(orderDao.getNotAfter() != null) {
            this.setNotAfter(DateUtil.asDate(orderDao.getNotAfter()));
        }
		this.setCertificate(certificateUrl);
	}

	public OrderResponse() {
		super();
	}

	/**
	 * @return the notBefore
	 */
	@JsonFormat (shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss'Z'")
	public Date getNotBefore() {
		return notBefore;
	}

	/**
	 * @param notBefore the notBefore to set
	 */
	public void setNotBefore(Date notBefore) {
		if( notBefore != null) {
			this.notBefore = notBefore;
		}
	}

	/**
	 * @return the notAfter
	 */
	@JsonFormat (shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss'Z'")
	public Date getNotAfter() {
		return notAfter;
	}

	/**
	 * @param notAfter the notAfter to set
	 */
	public void setNotAfter(Date notAfter) {
		if( notAfter != null) {
		  this.notAfter = notAfter;
		}
	}

	/**
	 * @return the certificate
	 */
	public String getCertificate() {
		return certificate;
	}

	/**
	 * @param certificate the certificate to set
	 */
	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}


  }

