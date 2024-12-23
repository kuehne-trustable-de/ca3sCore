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

import java.io.File;
import java.util.Objects;
/**
 * IssuerDnImportcrlBody
 */

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2024-02-27T11:17:53.164838525Z[GMT]")

public class IssuerDnImportcrlBody {
  @JsonProperty("crlPartitionIndex")
  private Integer crlPartitionIndex = 0;

  @JsonProperty("crlFile")
  private File crlFile = null;

  public IssuerDnImportcrlBody crlPartitionIndex(Integer crlPartitionIndex) {
    this.crlPartitionIndex = crlPartitionIndex;
    return this;
  }

   /**
   * CRL partition index
   * @return crlPartitionIndex
  **/
  @Schema(description = "CRL partition index")
  public Integer getCrlPartitionIndex() {
    return crlPartitionIndex;
  }

  public void setCrlPartitionIndex(Integer crlPartitionIndex) {
    this.crlPartitionIndex = crlPartitionIndex;
  }

  public IssuerDnImportcrlBody crlFile(File crlFile) {
    this.crlFile = crlFile;
    return this;
  }

   /**
   * CRL file in DER format
   * @return crlFile
  **/
  @Schema(description = "CRL file in DER format")
  public File getCrlFile() {
    return crlFile;
  }

  public void setCrlFile(File crlFile) {
    this.crlFile = crlFile;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IssuerDnImportcrlBody issuerDnImportcrlBody = (IssuerDnImportcrlBody) o;
    return Objects.equals(this.crlPartitionIndex, issuerDnImportcrlBody.crlPartitionIndex) &&
        Objects.equals(this.crlFile, issuerDnImportcrlBody.crlFile);
  }

  @Override
  public int hashCode() {
    return Objects.hash(crlPartitionIndex, Objects.hashCode(crlFile));
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IssuerDnImportcrlBody {\n");

    sb.append("    crlPartitionIndex: ").append(toIndentedString(crlPartitionIndex)).append("\n");
    sb.append("    crlFile: ").append(toIndentedString(crlFile)).append("\n");
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
