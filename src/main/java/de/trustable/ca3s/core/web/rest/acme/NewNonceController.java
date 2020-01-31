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

import static org.springframework.http.CacheControl.noStore;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import de.trustable.ca3s.core.domain.AcmeNonce;


@Controller
@RequestMapping("/acme/{realm}/newNonce")
/**
 * Produces <a href="https://de.wikipedia.org/wiki/Nonce">Nonces</a> containing
 * 16 random bytes followed by 8 bytes from epoch seconds.
 */
public class NewNonceController extends ACMEController {

	public static final String REPLAY_NONCE_HEADER = "Replay-Nonce";

	private static final Logger LOG = LoggerFactory.getLogger(NewNonceController.class);

	@RequestMapping(method = { GET, POST, HEAD })
	public ResponseEntity<String> viaGet() {
		LOG.info("New NONCE requested");

		AcmeNonce nonce = getNewNonce();

		return ResponseEntity.noContent().header(REPLAY_NONCE_HEADER, nonce.getNonceValue())
				.cacheControl(noStore()).build();
	}

	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void cleanupNonces() {
		long startTime = System.currentTimeMillis();
		List<AcmeNonce> expiredNonceList = nonceRepository.findByNonceExpiryDate(new java.util.Date());
		if(expiredNonceList.isEmpty()) {
			return;
		}
		
		LOG.debug("CleanupScheduler.cleanupNonce called ...");
		for (AcmeNonce nonce : expiredNonceList) {
			LOG.debug("cleanupNonce {} deleting", nonce.getNonceValue());
			
			nonceRepository.delete(nonce);
		}
		
		LOG.debug("CleanupScheduler.cleanupNonce finishes in {} ms", System.currentTimeMillis() - startTime);
	}

}
