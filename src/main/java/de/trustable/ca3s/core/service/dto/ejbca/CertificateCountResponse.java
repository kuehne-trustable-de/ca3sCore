/*
 * EJBCA REST Interface
 * API reference documentation.
 *
 * OpenAPI spec version: 1.0
 *
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package de.trustable.ca3s.core.service.dto.ejbca;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;
/**
 * CertificateCountResponse
 */

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2024-02-27T11:17:53.164838525Z[GMT]")

public class CertificateCountResponse {
  @JsonProperty("count")
  private Long count = null;

  public CertificateCountResponse count(Long count) {
    this.count = count;
    return this;
  }

   /**
   * The quantity of issued or active certificates
   * @return count
  **/
  @Schema(example = "1054", description = "The quantity of issued or active certificates")
  public Long getCount() {
    return count;
  }

  public void setCount(Long count) {
    this.count = count;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CertificateCountResponse certificateCountResponse = (CertificateCountResponse) o;
    return Objects.equals(this.count, certificateCountResponse.count);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CertificateCountResponse {\n");

    sb.append("    count: ").append(toIndentedString(count)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}
