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

package de.trustable.ca3sjh.web.rest.acme;


import javax.annotation.concurrent.NotThreadSafe;
import java.net.URI;
import java.net.URL;

@NotThreadSafe
public final class AccountDoesNotExistException extends RuntimeException {

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

public AccountDoesNotExistException(final long accountId, final Exception cause) {
    super("ID '" + accountId + "'", cause);
  }

  public AccountDoesNotExistException(final URL accountUrl, final Exception cause) {
    super("URL '" + accountUrl + "'", cause);
  }

  public AccountDoesNotExistException(final URI accountUri, final Exception cause) {
    super("URI '" + accountUri + "'", cause);
  }

  public AccountDoesNotExistException(final long accountId) {
    this(accountId, null);
  }

  public AccountDoesNotExistException(final URL accountUrl) {
    this(accountUrl, null);
  }

  public AccountDoesNotExistException(final URI accountUri) {
    this(accountUri, null);
  }

}
