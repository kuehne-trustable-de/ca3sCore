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

import javax.annotation.concurrent.Immutable;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.trustable.ca3s.core.domain.ACMEAccount;
import de.trustable.ca3s.core.domain.AcmeContact;
import de.trustable.ca3s.core.domain.enumeration.AccountStatus;
import org.springframework.web.util.UriComponentsBuilder;


@Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountResponse {

	@Transient
	transient Logger logger = LoggerFactory.getLogger(AccountResponse.class);

	@JsonProperty("id")
	private long id;

	@JsonProperty("status")
	private AccountStatus status;

	@JsonProperty("contact")
    private String[] contacts;

	@JsonProperty("termsOfServiceAgreed")
	private boolean termsAgreed = false;

	@JsonProperty("orders")
	private String orders;


	public AccountResponse(ACMEAccount accountDao, UriComponentsBuilder uriComponentsBuilder) {
		this.setId( accountDao.getAccountId());
		this.setStatus(accountDao.getStatus());
		String[] contacts = new String[accountDao.getContacts().size()];
		int i = 0;
		for( AcmeContact contactDao: accountDao.getContacts()) {
			contacts[i++] = contactDao.getContactUrl();
		}
		this.setContacts(contacts);
		this.setTermsAgreed(accountDao.isTermsOfServiceAgreed());
		this.setOrders(uriComponentsBuilder.path("/../").path(accountDao.getAccountId().toString()).path("/orders").build().normalize().toString());
        logger.info("AccountResponse has ordersUrl '{}'", this.getOrders());
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}



	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}


	/**
	 * @return the status
	 */
	public AccountStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(AccountStatus status) {
		this.status = status;
	}

	/**
	 * @return the contacts
	 */
	public String[] getContacts() {
		return contacts;
	}

	/**
	 * @param contacts the contacts to set
	 */
	public void setContacts(String[] contacts) {
		this.contacts = contacts;
	}

	/**
	 * @return the termsAgreed
	 */
	public boolean isTermsAgreed() {
		return termsAgreed;
	}

	/**
	 * @param termsAgreed the termsAgreed to set
	 */
	public void setTermsAgreed(boolean termsAgreed) {
		this.termsAgreed = termsAgreed;
	}

	/**
	 * @return the orders
	 */
	public String getOrders() {
		return orders;
	}

	/**
	 * @param orders the orders to set
	 */
	public void setOrders(String orders) {
		this.orders = orders;
	}


}

