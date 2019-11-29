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

import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

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
public class OrderSetResponse {

	@JsonProperty("orders")
    private String[] orderUrls = null;


	public OrderSetResponse() {}
	

	/**
	 * @return the orderUrls
	 */
	public String[] getOrderUrls() {
		return orderUrls;
	}

	/**
	 * @param orderUrls the orderUrls to set
	 */
	public void setOrderUrls(String[] orderUrls) {
		this.orderUrls = orderUrls;
	}
	
	
}

