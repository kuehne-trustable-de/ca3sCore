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
import org.threeten.bp.OffsetDateTime;

import java.util.Objects;
/**
 * RevokeStatusRestResponse
 */

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2024-02-27T11:17:53.164838525Z[GMT]")

public class RevokeStatusRestResponse {
  @JsonProperty("issuer_dn")
  private String issuerDn = null;

  @JsonProperty("serial_number")
  private String serialNumber = null;

  @JsonProperty("revocation_reason")
  private String revocationReason = null;

  @JsonProperty("revocation_date")
  private OffsetDateTime revocationDate = null;

  @JsonProperty("invalidity_date")
  private OffsetDateTime invalidityDate = null;

  @JsonProperty("message")
  private String message = null;

  @JsonProperty("revoked")
  private Boolean revoked = null;

  public RevokeStatusRestResponse issuerDn(String issuerDn) {
    this.issuerDn = issuerDn;
    return this;
  }

   /**
   * Issuer Distinguished Name
   * @return issuerDn
  **/
  @Schema(example = "CN=ExampleCA", description = "Issuer Distinguished Name")
  public String getIssuerDn() {
    return issuerDn;
  }

  public void setIssuerDn(String issuerDn) {
    this.issuerDn = issuerDn;
  }

  public RevokeStatusRestResponse serialNumber(String serialNumber) {
    this.serialNumber = serialNumber;
    return this;
  }

   /**
   * Hex Serial Number
   * @return serialNumber
  **/
  @Schema(example = "1234567890ABCDEF", description = "Hex Serial Number")
  public String getSerialNumber() {
    return serialNumber;
  }

  public void setSerialNumber(String serialNumber) {
    this.serialNumber = serialNumber;
  }

  public RevokeStatusRestResponse revocationReason(String revocationReason) {
    this.revocationReason = revocationReason;
    return this;
  }

   /**
   * RFC5280 revokation reason
   * @return revocationReason
  **/
  @Schema(example = "KEY_COMPROMISE", description = "RFC5280 revokation reason")
  public String getRevocationReason() {
    return revocationReason;
  }

  public void setRevocationReason(String revocationReason) {
    this.revocationReason = revocationReason;
  }

  public RevokeStatusRestResponse revocationDate(OffsetDateTime revocationDate) {
    this.revocationDate = revocationDate;
    return this;
  }

   /**
   * Revokation date
   * @return revocationDate
  **/
  @Schema(example = "1970-01-01T00:00Z", description = "Revokation date")
  public OffsetDateTime getRevocationDate() {
    return revocationDate;
  }

  public void setRevocationDate(OffsetDateTime revocationDate) {
    this.revocationDate = revocationDate;
  }

  public RevokeStatusRestResponse invalidityDate(OffsetDateTime invalidityDate) {
    this.invalidityDate = invalidityDate;
    return this;
  }

   /**
   * Invalidity date
   * @return invalidityDate
  **/
  @Schema(example = "1970-01-01T00:00Z", description = "Invalidity date")
  public OffsetDateTime getInvalidityDate() {
    return invalidityDate;
  }

  public void setInvalidityDate(OffsetDateTime invalidityDate) {
    this.invalidityDate = invalidityDate;
  }

  public RevokeStatusRestResponse message(String message) {
    this.message = message;
    return this;
  }

   /**
   * Message
   * @return message
  **/
  @Schema(example = "Successfully revoked", description = "Message")
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public RevokeStatusRestResponse revoked(Boolean revoked) {
    this.revoked = revoked;
    return this;
  }

   /**
   * Get revoked
   * @return revoked
  **/
  @Schema(description = "")
  public Boolean isRevoked() {
    return revoked;
  }

  public void setRevoked(Boolean revoked) {
    this.revoked = revoked;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RevokeStatusRestResponse revokeStatusRestResponse = (RevokeStatusRestResponse) o;
    return Objects.equals(this.issuerDn, revokeStatusRestResponse.issuerDn) &&
        Objects.equals(this.serialNumber, revokeStatusRestResponse.serialNumber) &&
        Objects.equals(this.revocationReason, revokeStatusRestResponse.revocationReason) &&
        Objects.equals(this.revocationDate, revokeStatusRestResponse.revocationDate) &&
        Objects.equals(this.invalidityDate, revokeStatusRestResponse.invalidityDate) &&
        Objects.equals(this.message, revokeStatusRestResponse.message) &&
        Objects.equals(this.revoked, revokeStatusRestResponse.revoked);
  }

  @Override
  public int hashCode() {
    return Objects.hash(issuerDn, serialNumber, revocationReason, revocationDate, invalidityDate, message, revoked);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RevokeStatusRestResponse {\n");

    sb.append("    issuerDn: ").append(toIndentedString(issuerDn)).append("\n");
    sb.append("    serialNumber: ").append(toIndentedString(serialNumber)).append("\n");
    sb.append("    revocationReason: ").append(toIndentedString(revocationReason)).append("\n");
    sb.append("    revocationDate: ").append(toIndentedString(revocationDate)).append("\n");
    sb.append("    invalidityDate: ").append(toIndentedString(invalidityDate)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    revoked: ").append(toIndentedString(revoked)).append("\n");
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
