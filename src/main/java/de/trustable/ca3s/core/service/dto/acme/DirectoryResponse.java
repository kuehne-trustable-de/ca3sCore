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

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonProperty;

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
     "meta": {
       "termsOfService": "https://example.com/acme/terms/2017-5-30",
       "website": "https://www.example.com/",
       "caaIdentities": ["example.com"],
       "externalAccountRequired": false
     }
   }
 */


public class DirectoryResponse {

	private URI newNonceUri, newAccountUri, newOrderUri, newAuthzUri, revokeUri, keyChangeUri;

	/**
	 * @return Never <code>null</code>
	 */
	@JsonProperty("newNonce")
	public URI getNewNonceUri() {
		return newNonceUri;
	}

	/**
	 * @return Never <code>null</code>
	 */
	@JsonProperty("newAccount")
	public URI getNewAccountUri() {
		return newAccountUri;
	}

	/**
	 * @return Never <code>null</code>
	 */
	@JsonProperty("newOrder")
	public URI getNewOrderUri() {
		return newOrderUri;
	}

	/**
	 * @return Never <code>null</code>
	 */
	@JsonProperty("newAuthz")
	public URI getNewAuthzUri() {
		return newAuthzUri;
	}

	/**
	 * @return Never <code>null</code>
	 */
	@JsonProperty("revokeCert")
	public URI getRevokeCertUri() {
		return revokeUri;
	}

	/**
	 * @return Never <code>null</code>
	 */
	@JsonProperty("keyChange")
	public URI getkeyChangeUri() {
		return keyChangeUri;
	}

	/**
	 * @param newNonceUri the newNonceUri to set
	 */
	public void setNewNonceUri(URI newNonceUri) {
		this.newNonceUri = newNonceUri;
	}

	/**
	 * @param newAccountUri the newAccountUri to set
	 */
	public void setNewAccountUri(URI newAccountUri) {
		this.newAccountUri = newAccountUri;
	}

	/**
	 * @param newOrderUri the newOrderUri to set
	 */
	public void setNewOrderUri(URI newOrderUri) {
		this.newOrderUri = newOrderUri;
	}

	/**
	 * @param newAuthzUri the newAuthzUri to set
	 */
	public void setNewAuthzUri(URI newAuthzUri) {
		this.newAuthzUri = newAuthzUri;
	}

	/**
	 * @param revokeUri the revokeUri to set
	 */
	public void setRevokeUri(URI revokeUri) {
		this.revokeUri = revokeUri;
	}

	/**
	 * @param keyChangeUri the keyChangeUri to set
	 */
	public void setKeyChangeUri(URI keyChangeUri) {
		this.keyChangeUri = keyChangeUri;
	}

	
}
