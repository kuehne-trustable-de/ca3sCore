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

package de.trustable.ca3s.core.web.rest.acme;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequestUri;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.service.dto.acme.DirectoryResponse;

/*
 * 7.1.1.  Directory

   In order to help clients configure themselves with the right URLs for
   each ACME operation, ACME servers provide a directory object.  This
   should be the only URL needed to configure clients.  It is a JSON
   object, whose field names are drawn from the resource registry
   (Section 9.7.5) and whose values are the corresponding URLs.

                    +------------+--------------------+
                    | Field      | URL in value       |
                    +------------+--------------------+
                    | newNonce   | New nonce          |
                    |            |                    |
                    | newAccount | New account        |
                    |            |                    |
                    | newOrder   | New order          |
                    |            |                    |
                    | newAuthz   | New authorization  |
                    |            |                    |
                    | revokeCert | Revoke certificate |
                    |            |                    |
                    | keyChange  | Key Change         |
                    +------------+--------------------+

   There is no constraint on the URL of the directory except that it
   should be different from the other ACME server resources' URLs, and
   that it should not clash with other services.  For instance:

   o  a host which functions as both an ACME and a Web server may want
      to keep the root path "/" for an HTML "front page", and place the
      ACME directory under the path "/acme".

   o  a host which only functions as an ACME server could place the
      directory under the path "/".

   If the ACME server does not implement pre-authorization
   (Section 7.4.1) it MUST omit the "newAuthz" field of the directory.

   The object MAY additionally contain a field "meta".  If present, it
   MUST be a JSON object; each field in the object is an item of
   metadata relating to the service provided by the ACME server.

   The following metadata items are defined (Section 9.7.6), all of
   which are OPTIONAL:

   termsOfService (optional, string):  A URL identifying the current
      terms of service.

   website (optional, string):  An HTTP or HTTPS URL locating a website
      providing more information about the ACME server.

   caaIdentities (optional, array of string):  The hostnames that the
      ACME server recognizes as referring to itself for the purposes of
      CAA record validation as defined in [RFC6844].  Each string MUST
      represent the same sequence of ASCII code points that the server
      will expect to see as the "Issuer Domain Name" in a CAA issue or
      issuewild property tag.  This allows clients to determine the
      correct issuer domain name to use when configuring CAA records.

   externalAccountRequired (optional, boolean):  If this field is
      present and set to "true", then the CA requires that all new-
      account requests include an "externalAccountBinding" field
      associating the new account with an external account.

   Clients access the directory by sending a GET request to the
   directory URL.

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

@Controller
@RequestMapping("/acme/{realm}/directory")
public class DirectoryController extends ACMEController {

	private static final Logger LOG = LoggerFactory.getLogger(DirectoryController.class);

	@RequestMapping(method = { GET, POST }, produces = APPLICATION_JSON_VALUE)
	public @ResponseBody DirectoryResponse getDirectory(@PathVariable final String realm) {

		// check for existence of a pipeline for the realm
		getPipelineForRealm(realm);

		DirectoryResponse resp = new DirectoryResponse();

		resp.setNewNonceUri(newNonceResourceUriBuilderFrom(fromCurrentRequestUri()).build().normalize().toUri());
		resp.setNewAccountUri(newAccountResourceUriBuilderFrom(fromCurrentRequestUri()).build().normalize().toUri());
		resp.setNewOrderUri(newOrderResourceUriBuilderFrom(fromCurrentRequestUri()).build().normalize().toUri());
		resp.setNewAuthzUri(newAuthorizationResourceUriBuilderFrom(fromCurrentRequestUri()).build().normalize().toUri());

		resp.setRevokeUri(revokeResourceUriBuilderFrom(fromCurrentRequestUri()).build().normalize().toUri());
		resp.setKeyChangeUri(keyChangeResourceUriBuilderFrom(fromCurrentRequestUri()).build().normalize().toUri());

		LOG.info("directory request, returning {}", resp);
		return resp;
	}

}
