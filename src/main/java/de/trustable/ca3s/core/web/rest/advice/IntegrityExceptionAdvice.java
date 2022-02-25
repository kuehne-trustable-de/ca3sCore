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
import de.trustable.ca3s.core.service.exception.IntegrityException;
import de.trustable.ca3s.core.service.util.ACMEUtil;
import de.trustable.ca3s.core.web.rest.acme.ACMEController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.annotation.concurrent.Immutable;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * Handle the restification of a MessagingException
 *
 * @author kuehn
 *
 */
@ControllerAdvice
@Immutable
public final class IntegrityExceptionAdvice {

	@ExceptionHandler(value = IntegrityException.class)
	public ResponseEntity<ProblemDetail> respondTo(final IntegrityException exception) {

        final ProblemDetail problem = new ProblemDetail(ACMEUtil.SERVER_INTERNAL, "Data integrity problem",
            BAD_REQUEST, exception.getMessage(), ACMEUtil.NO_INSTANCE);
        final HttpStatus status = problem.getStatus();
		return ResponseEntity.status(status).contentType(ACMEController.APPLICATION_PROBLEM_JSON).body(problem);
	}
}
