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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
/**
 * CertificateProfileInfoRestResponseV2
 */

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2024-02-27T11:17:53.164838525Z[GMT]")

public class CertificateProfileInfoRestResponseV2 {
  @JsonProperty("certificate_profile_id")
  private Integer certificateProfileId = null;

  @JsonProperty("available_key_algs")
  private List<String> availableKeyAlgs = null;

  @JsonProperty("available_bit_lenghts")
  private List<Integer> availableBitLenghts = null;

  @JsonProperty("available_security_levels")
  private List<Integer> availableSecurityLevels = null;

  @JsonProperty("available_ecdsa_curves")
  private List<String> availableEcdsaCurves = null;

  @JsonProperty("available_cas")
  private List<String> availableCas = null;

  public CertificateProfileInfoRestResponseV2 certificateProfileId(Integer certificateProfileId) {
    this.certificateProfileId = certificateProfileId;
    return this;
  }

   /**
   * Get certificateProfileId
   * @return certificateProfileId
  **/
  @Schema(description = "")
  public Integer getCertificateProfileId() {
    return certificateProfileId;
  }

  public void setCertificateProfileId(Integer certificateProfileId) {
    this.certificateProfileId = certificateProfileId;
  }

  public CertificateProfileInfoRestResponseV2 availableKeyAlgs(List<String> availableKeyAlgs) {
    this.availableKeyAlgs = availableKeyAlgs;
    return this;
  }

  public CertificateProfileInfoRestResponseV2 addAvailableKeyAlgsItem(String availableKeyAlgsItem) {
    if (this.availableKeyAlgs == null) {
      this.availableKeyAlgs = new ArrayList<String>();
    }
    this.availableKeyAlgs.add(availableKeyAlgsItem);
    return this;
  }

   /**
   * Get availableKeyAlgs
   * @return availableKeyAlgs
  **/
  @Schema(description = "")
  public List<String> getAvailableKeyAlgs() {
    return availableKeyAlgs;
  }

  public void setAvailableKeyAlgs(List<String> availableKeyAlgs) {
    this.availableKeyAlgs = availableKeyAlgs;
  }

  public CertificateProfileInfoRestResponseV2 availableBitLenghts(List<Integer> availableBitLenghts) {
    this.availableBitLenghts = availableBitLenghts;
    return this;
  }

  public CertificateProfileInfoRestResponseV2 addAvailableBitLenghtsItem(Integer availableBitLenghtsItem) {
    if (this.availableBitLenghts == null) {
      this.availableBitLenghts = new ArrayList<Integer>();
    }
    this.availableBitLenghts.add(availableBitLenghtsItem);
    return this;
  }

   /**
   * Get availableBitLenghts
   * @return availableBitLenghts
  **/
  @Schema(description = "")
  public List<Integer> getAvailableBitLenghts() {
    return availableBitLenghts;
  }

  public void setAvailableBitLenghts(List<Integer> availableBitLenghts) {
    this.availableBitLenghts = availableBitLenghts;
  }

  public CertificateProfileInfoRestResponseV2 availableSecurityLevels(List<Integer> availableSecurityLevels) {
    this.availableSecurityLevels = availableSecurityLevels;
    return this;
  }

  public CertificateProfileInfoRestResponseV2 addAvailableSecurityLevelsItem(Integer availableSecurityLevelsItem) {
    if (this.availableSecurityLevels == null) {
      this.availableSecurityLevels = new ArrayList<Integer>();
    }
    this.availableSecurityLevels.add(availableSecurityLevelsItem);
    return this;
  }

   /**
   * Get availableSecurityLevels
   * @return availableSecurityLevels
  **/
  @Schema(description = "")
  public List<Integer> getAvailableSecurityLevels() {
    return availableSecurityLevels;
  }

  public void setAvailableSecurityLevels(List<Integer> availableSecurityLevels) {
    this.availableSecurityLevels = availableSecurityLevels;
  }

  public CertificateProfileInfoRestResponseV2 availableEcdsaCurves(List<String> availableEcdsaCurves) {
    this.availableEcdsaCurves = availableEcdsaCurves;
    return this;
  }

  public CertificateProfileInfoRestResponseV2 addAvailableEcdsaCurvesItem(String availableEcdsaCurvesItem) {
    if (this.availableEcdsaCurves == null) {
      this.availableEcdsaCurves = new ArrayList<String>();
    }
    this.availableEcdsaCurves.add(availableEcdsaCurvesItem);
    return this;
  }

   /**
   * Get availableEcdsaCurves
   * @return availableEcdsaCurves
  **/
  @Schema(description = "")
  public List<String> getAvailableEcdsaCurves() {
    return availableEcdsaCurves;
  }

  public void setAvailableEcdsaCurves(List<String> availableEcdsaCurves) {
    this.availableEcdsaCurves = availableEcdsaCurves;
  }

  public CertificateProfileInfoRestResponseV2 availableCas(List<String> availableCas) {
    this.availableCas = availableCas;
    return this;
  }

  public CertificateProfileInfoRestResponseV2 addAvailableCasItem(String availableCasItem) {
    if (this.availableCas == null) {
      this.availableCas = new ArrayList<String>();
    }
    this.availableCas.add(availableCasItem);
    return this;
  }

   /**
   * Get availableCas
   * @return availableCas
  **/
  @Schema(description = "")
  public List<String> getAvailableCas() {
    return availableCas;
  }

  public void setAvailableCas(List<String> availableCas) {
    this.availableCas = availableCas;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CertificateProfileInfoRestResponseV2 certificateProfileInfoRestResponseV2 = (CertificateProfileInfoRestResponseV2) o;
    return Objects.equals(this.certificateProfileId, certificateProfileInfoRestResponseV2.certificateProfileId) &&
        Objects.equals(this.availableKeyAlgs, certificateProfileInfoRestResponseV2.availableKeyAlgs) &&
        Objects.equals(this.availableBitLenghts, certificateProfileInfoRestResponseV2.availableBitLenghts) &&
        Objects.equals(this.availableSecurityLevels, certificateProfileInfoRestResponseV2.availableSecurityLevels) &&
        Objects.equals(this.availableEcdsaCurves, certificateProfileInfoRestResponseV2.availableEcdsaCurves) &&
        Objects.equals(this.availableCas, certificateProfileInfoRestResponseV2.availableCas);
  }

  @Override
  public int hashCode() {
    return Objects.hash(certificateProfileId, availableKeyAlgs, availableBitLenghts, availableSecurityLevels, availableEcdsaCurves, availableCas);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CertificateProfileInfoRestResponseV2 {\n");

    sb.append("    certificateProfileId: ").append(toIndentedString(certificateProfileId)).append("\n");
    sb.append("    availableKeyAlgs: ").append(toIndentedString(availableKeyAlgs)).append("\n");
    sb.append("    availableBitLenghts: ").append(toIndentedString(availableBitLenghts)).append("\n");
    sb.append("    availableSecurityLevels: ").append(toIndentedString(availableSecurityLevels)).append("\n");
    sb.append("    availableEcdsaCurves: ").append(toIndentedString(availableEcdsaCurves)).append("\n");
    sb.append("    availableCas: ").append(toIndentedString(availableCas)).append("\n");
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
