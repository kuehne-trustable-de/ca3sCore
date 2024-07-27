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

package de.trustable.ca3s.core.web.rest.advice;

import de.trustable.ca3s.core.security.IPBlockedException;
import de.trustable.ca3s.core.security.UserCredentialsExpiredException;
import de.trustable.ca3s.core.service.dto.acme.problem.ProblemDetail;
import de.trustable.ca3s.core.service.exception.BlockedCredentialsException;
import de.trustable.ca3s.core.service.util.AcmeUtil;
import de.trustable.ca3s.core.web.rest.acme.AcmeController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.annotation.concurrent.Immutable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static de.trustable.ca3s.core.web.rest.errors.RestURIs.CREDENTIALS_EXPIRED;
import static de.trustable.ca3s.core.web.rest.errors.RestURIs.USER_BLOCKED;

/**
 * Handle the restification of an Exception
 *
 * @author kuehn
 *
 */
@ControllerAdvice
@Immutable
public final class BlockedCredentialsAdvice {

    @ExceptionHandler(value = BlockedCredentialsException.class)
    public ResponseEntity<ProblemDetail> respondTo(final BlockedCredentialsException exception) {

        final ProblemDetail problem = new ProblemDetail(USER_BLOCKED, "User blocked",
            HttpStatus.UNAUTHORIZED,
            "" + exception.getBlockedUntilDate(), AcmeUtil.NO_INSTANCE);
        final HttpStatus status = problem.getStatus();
        return ResponseEntity.status(status).contentType(AcmeController.APPLICATION_PROBLEM_JSON).body(problem);
    }

    @ExceptionHandler(value = IPBlockedException.class)
    public ResponseEntity<ProblemDetail> respondTo(final IPBlockedException exception) {

        final ProblemDetail problem = new ProblemDetail(USER_BLOCKED, "User blocked",
            HttpStatus.UNAUTHORIZED,
            "" + Instant.now().plus(600, ChronoUnit.SECONDS), AcmeUtil.NO_INSTANCE);
        final HttpStatus status = problem.getStatus();
        return ResponseEntity.status(status).contentType(AcmeController.APPLICATION_PROBLEM_JSON).body(problem);
    }


    @ExceptionHandler(value = UserCredentialsExpiredException.class)
    public ResponseEntity<ProblemDetail> respondTo(final UserCredentialsExpiredException exception) {

        final ProblemDetail problem = new ProblemDetail(CREDENTIALS_EXPIRED, "Credentials expired",
            HttpStatus.UNAUTHORIZED,
            "", AcmeUtil.NO_INSTANCE);
        final HttpStatus status = problem.getStatus();
        return ResponseEntity.status(status).contentType(AcmeController.APPLICATION_PROBLEM_JSON).body(problem);
    }

}
