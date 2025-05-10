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

import de.trustable.ca3s.core.service.dto.acme.problem.ProblemDetail;
import de.trustable.ca3s.core.service.util.AcmeUtil;
import de.trustable.ca3s.core.web.rest.acme.AcmeController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.annotation.concurrent.Immutable;

/**
 * Handle the restification of an Exception
 *
 * @author kuehn
 *
 */
@ControllerAdvice
@Immutable
public final class UserProblemsAdvice {

    @ExceptionHandler(value = BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> respondTo(final BadCredentialsException exception) {

        final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, "Bad credentials",
            HttpStatus.FORBIDDEN, exception.getMessage(), AcmeUtil.NO_INSTANCE);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(AcmeController.APPLICATION_PROBLEM_JSON).body(problem);
    }

    @ExceptionHandler(value = AuthenticationException.class)
    public ResponseEntity<ProblemDetail> respondTo(final AuthenticationException exception) {

        final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, "Authentication problem",
            HttpStatus.FORBIDDEN, exception.getMessage(), AcmeUtil.NO_INSTANCE);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(AcmeController.APPLICATION_PROBLEM_JSON).body(problem);
    }

}
