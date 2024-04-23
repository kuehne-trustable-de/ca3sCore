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

import com.google.gson.annotations.SerializedName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;
/**
 * RestResourceStatusRestResponse
 */

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2024-02-27T11:17:53.164838525Z[GMT]")

public class RestResourceStatusRestResponse {
  @SerializedName("status")
  private String status = null;

  @SerializedName("version")
  private String version = null;

  @SerializedName("revision")
  private String revision = null;

  public RestResourceStatusRestResponse status(String status) {
    this.status = status;
    return this;
  }

   /**
   * Status
   * @return status
  **/
  @Schema(example = "OK", description = "Status")
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public RestResourceStatusRestResponse version(String version) {
    this.version = version;
    return this;
  }

   /**
   * Resource version
   * @return version
  **/
  @Schema(example = "1.0", description = "Resource version")
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public RestResourceStatusRestResponse revision(String revision) {
    this.revision = revision;
    return this;
  }

   /**
   * Application revision
   * @return revision
  **/
  @Schema(example = "EJBCA 1.0.0 Enterprise", description = "Application revision")
  public String getRevision() {
    return revision;
  }

  public void setRevision(String revision) {
    this.revision = revision;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RestResourceStatusRestResponse restResourceStatusRestResponse = (RestResourceStatusRestResponse) o;
    return Objects.equals(this.status, restResourceStatusRestResponse.status) &&
        Objects.equals(this.version, restResourceStatusRestResponse.version) &&
        Objects.equals(this.revision, restResourceStatusRestResponse.revision);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, version, revision);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RestResourceStatusRestResponse {\n");

    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    revision: ").append(toIndentedString(revision)).append("\n");
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