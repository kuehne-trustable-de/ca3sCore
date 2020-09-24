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

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequestUri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Optional;

import org.jose4j.jwt.consumer.JwtContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UriComponentsBuilder;

import de.trustable.ca3s.core.domain.ACMEAccount;
import de.trustable.ca3s.core.domain.AcmeChallenge;
import de.trustable.ca3s.core.domain.enumeration.ChallengeStatus;
import de.trustable.ca3s.core.repository.AcmeChallengeRepository;
import de.trustable.ca3s.core.service.dto.acme.ChallengeResponse;
import de.trustable.ca3s.core.service.dto.acme.problem.AcmeProblemException;
import de.trustable.ca3s.core.service.dto.acme.problem.ProblemDetail;
import de.trustable.ca3s.core.service.util.ACMEUtil;
import de.trustable.ca3s.core.service.util.PreferenceUtil;


@Controller
@RequestMapping("/acme/{realm}/challenge")
public class ChallengeController extends ACMEController {

    private static final Logger LOG = LoggerFactory.getLogger(ChallengeController.class);

    @Autowired
    private AcmeChallengeRepository challengeRepository;

    /*
	@Value("${acmeClientPorts:80, 5544, 8800}")
	private String portList;
*/
    
	@Autowired
	private PreferenceUtil preferenceUtil;

	
    @RequestMapping(value = "/{challengeId}", method = GET, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getChallenge(@PathVariable final long challengeId) {
  	  
	  	LOG.debug("Received Challenge request ");

	    final HttpHeaders additionalHeaders = buildNonceHeader();
		

		Optional<AcmeChallenge> challengeOpt = challengeRepository.findById(challengeId);
		if(!challengeOpt.isPresent()) {
		    return ResponseEntity.notFound().headers(additionalHeaders).build();   
		}else {
			AcmeChallenge challengeDao = challengeOpt.get();
	
			LOG.debug( "returning challenge {}", challengeDao.getId());
	
			ChallengeResponse challenge = buildChallengeResponse(challengeDao);
	
			if(challengeDao.getStatus() == ChallengeStatus.VALID ) {
				URI authUri = locationUriOfAuthorization(challengeDao.getAcmeAuthorization().getAcmeAuthorizationId(), fromCurrentRequestUri());
			    additionalHeaders.set("Link", "<" + authUri.toASCIIString() + ">;rel=\"up\"");
			    return ok().headers(additionalHeaders).body(challenge);
			}else {
			    return ok().headers(additionalHeaders).body(challenge);
			}
		}
	
    }
    
  @RequestMapping(value = "/{challengeId}", method = POST, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JOSE_JSON_VALUE)
  public ResponseEntity<?> postChallenge(@RequestBody final String requestBody,
		  @PathVariable final long challengeId) {
	  
	LOG.debug("Received Challenge request ");
	
	try {
		JwtContext context = jwtUtil.processFlattenedJWT(requestBody);
	   
		ACMEAccount acctDao = checkJWTSignatureForAccount(context);
	
	    final HttpHeaders additionalHeaders = buildNonceHeader();

		Optional<AcmeChallenge> challengeOpt = challengeRepository.findById(challengeId);
		if(!challengeOpt.isPresent()) {
		    return ResponseEntity.notFound().headers(additionalHeaders).build();
		    
		}else {
			AcmeChallenge challengeDao = challengeOpt.get();

			if( challengeDao.getAcmeAuthorization().getOrder().getAccount().getAccountId() != acctDao.getAccountId() ) {
				LOG.warn("Account of signing key {} does not match account id {} associated to given challenge{}", acctDao.getAccountId(), challengeDao.getAcmeAuthorization().getOrder().getAccount().getAccountId(), challengeId);
				final ProblemDetail problem = new ProblemDetail(ACMEUtil.MALFORMED, "Account / Auth mismatch",
						BAD_REQUEST, "", ACMEController.NO_INSTANCE);
				throw new AcmeProblemException(problem);
			}
			
			LOG.debug( "checking challenge {}", challengeDao.getId());

			boolean solved = false;
			if( "http-01".equals(challengeDao.getType())){
				solved = checkChallengeHttp(challengeDao);

				if( solved) {
					challengeDao.setStatus(ChallengeStatus.VALID);
				}else {
					challengeDao.setStatus(ChallengeStatus.INVALID);
				}
				
//				solved = true;
//				LOG.error("!!! ignoring callback outcome !!!");

				challengeDao.setValidated(Instant.now());
				challengeRepository.save(challengeDao);

				LOG.debug("challengeDao set to '{}' at {}", challengeDao.getStatus().toString(), challengeDao.getValidated());

			}else{
				LOG.warn("Unexpected type '{}' of challenge{}", challengeDao.getType(), challengeId);
			}


			ChallengeResponse challenge = buildChallengeResponse(challengeDao);


			if( solved) {
				URI authUri = locationUriOfAuthorization(challengeDao.getAcmeAuthorization().getAcmeAuthorizationId(), fromCurrentRequestUri());
			    additionalHeaders.set("Link", "<" + authUri.toASCIIString() + ">;rel=\"up\"");
			    return ok().headers(additionalHeaders).body(challenge);
			}else {
				LOG.warn("validation of challenge{} of type '{}' failed", challengeId, challengeDao.getType());
				
				return ResponseEntity.badRequest().headers(additionalHeaders).body(challenge);
			}
		}
		
	} catch (AcmeProblemException e) {
	    return buildProblemResponseEntity(e);
	}
  }

	private boolean checkChallengeHttp(AcmeChallenge challengeDao) {

		int[] ports = {80, 5544, 8800};

		long timeoutMilliSec = preferenceUtil.getAcmeHTTP01TimeoutMilliSec();
		String portList = preferenceUtil.getAcmeHTTP01CallbackPorts();
		
		if(portList != null && !portList.trim().isEmpty()) {
			String[] parts = portList.split(",");
			ports = new int[parts.length];
		    for( int i = 0; i < parts.length; i++) {
		    	ports[i] = -1;
		    	try {
		    		ports[i] = Integer.parseInt(parts[i].trim());
		    		LOG.debug("checkChallengeHttp port number '" + ports[i] + "' configured for HTTP callback");
		    	} catch( NumberFormatException nfe) {
					LOG.warn("checkChallengeHttp port number parsing fails for '" + ports[i] + "', ignoring", nfe);
		    	}
		    }
			
		}
		
	    String token = challengeDao.getToken();
	    String pkThumbprint = challengeDao.getAcmeAuthorization().getOrder().getAccount().getPublicKeyHash();
        String expectedContent = token + '.' + pkThumbprint;

	    String fileNamePath = "/.well-known/acme-challenge/" + token;
	    String host = challengeDao.getAcmeAuthorization().getValue();
	    
	    for( int port: ports) {

		    try {
				URL url = new URL("http", host, port, fileNamePath);
				LOG.debug("Opening connection to  : " + url);
	
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
		
				// Just wait for two seconds
				con.setConnectTimeout((int) timeoutMilliSec);
				con.setReadTimeout((int) timeoutMilliSec);
				
				// optional default is GET
				con.setRequestMethod("GET");
		
				// add request header
				con.setRequestProperty("User-Agent", "CA3S_ACME");
		
				int responseCode = con.getResponseCode();
				LOG.debug("\nSending 'GET' request to URL : " + url.toString());
				LOG.debug("Response Code : " + responseCode);
		
				if( responseCode != 200) {
					LOG.info("read challenge responded with unexpected code : " + responseCode);
					return false;
				}
				
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
		
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
	
				String actualContent = response.toString().trim();
				LOG.debug("read challenge response: " + actualContent);
				LOG.debug("expected content: '{}'", expectedContent);
				
				return ( expectedContent.equals( actualContent));
	
		    } catch(UnknownHostException uhe) {
				LOG.debug("unable to resolve hostname ", uhe);
				return false;
		    } catch(IOException ioe) {
				LOG.debug("exception occured reading challenge response on {}:{} for {}", host, port, challengeDao.getId(), ioe.getLocalizedMessage());
		    }
	    }
		return false;
	}

	ChallengeResponse buildChallengeResponse(final AcmeChallenge challengeDao){
		ChallengeResponse challenge = new ChallengeResponse(challengeDao, locationUriOfChallenge(challengeDao.getId(), fromCurrentRequestUri()).toString());
		
		return challenge;
  }
  
  private URI locationUriOfChallenge(final long challengeId, final UriComponentsBuilder uriBuilder) {
	    return challengeResourceUriBuilderFrom(uriBuilder.path("../..")).path("/").path(Long.toString(challengeId)).build().normalize().toUri();
	  }

  private URI locationUriOfAuthorization(final long authorizationId, final UriComponentsBuilder uriBuilder) {
	    return authorizationResourceUriBuilderFrom(uriBuilder.path("../..")).path("/").path("..").path("/").path(Long.toString(authorizationId)).build().normalize().toUri();
	  }

}
