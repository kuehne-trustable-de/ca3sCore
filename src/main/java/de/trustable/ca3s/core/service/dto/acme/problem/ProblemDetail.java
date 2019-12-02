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

package de.trustable.ca3s.core.service.dto.acme.problem;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.annotation.concurrent.Immutable;
import java.net.URI;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * Open for subclassing to build <a href="https://tools.ietf.org/html/rfc7807#section-3.2">Extension Members</a>.
 */
@Immutable
@JsonInclude(NON_EMPTY)
public class ProblemDetail {

  public static final MediaType APPLICATION_PROBLEM_JSON = new MediaType("application", "problem+json");

  private final URI type, instance;
  private final String title, detail;
  private final HttpStatus status;

  /**
   * @param type     Optional
   * @param title    Optional
   * @param status   Optional
   * @param detail   Optional
   * @param instance Optional
   */
  public ProblemDetail(final URI type, final String title, final HttpStatus status, final String detail, final URI
          instance) {
    this.type = type;
    this.title = title;
    this.detail = detail;
    this.instance = instance;
    this.status = status;
  }

  /**
   * @return Optional
   */
  @JsonProperty("type")
  public final URI getType() {
    return type;
  }

  /**
   * @return Optional
   */
  @JsonProperty("title")
  public final String getTitle() {
    return title;
  }

  /**
   * @return Optional
   */
  @JsonProperty("detail")
  public final String getDetail() {
    return detail;
  }

  /**
   * @return Optional
   */
  @JsonProperty("instance")
  public final URI getInstance() {
    return instance;
  }

  /**
   * @return Optional
   */
  @JsonIgnore
  public final HttpStatus getStatus() {
    return status;
  }

  /**
   * @return Optional
   */
  @JsonProperty("status")
  public final Integer getStatusCode() {
    return (status == null ? null : status.value());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProblemDetail)) return false;

    ProblemDetail that = (ProblemDetail) o;

    if (!type.equals(that.type)) return false;
    if (!instance.equals(that.instance)) return false;
    if (!title.equals(that.title)) return false;
    if (!detail.equals(that.detail)) return false;
    return status == that.status;
  }

  @Override
  public int hashCode() {
    int result = type.hashCode();
    result = 31 * result + instance.hashCode();
    result = 31 * result + title.hashCode();
    result = 31 * result + detail.hashCode();
    result = 31 * result + status.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "ProblemDetail{" +
            "type=" + type +
            ", title='" + title + '\'' +
            ", detail='" + detail + '\'' +
            ", instance=" + instance +
            ", status=" + status +
            '}';
  }

}
