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

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.IOException;
import java.util.Objects;
/**
 * Use one of allowed values as property and operation. Available propertiesUSERNAME  ISSUER_DN  SUBJECT_DN  EXTERNAL_ACCOUNT_BINDING_ID  END_ENTITY_PROFILE  CERTIFICATE_PROFILE  STATUS  TAG  TYPE  UPDATE_TIME  ISSUED_DATE  EXPIRE_DATE  REVOCATION_DATE   Available operationsASC  DESC
 */
@Schema(description = "Use one of allowed values as property and operation. Available propertiesUSERNAME  ISSUER_DN  SUBJECT_DN  EXTERNAL_ACCOUNT_BINDING_ID  END_ENTITY_PROFILE  CERTIFICATE_PROFILE  STATUS  TAG  TYPE  UPDATE_TIME  ISSUED_DATE  EXPIRE_DATE  REVOCATION_DATE   Available operationsASC  DESC  ")
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2024-02-27T11:17:53.164838525Z[GMT]")

public class SearchCertificateSortRestRequest {
  /**
   * Sorted by
   */
  @JsonAdapter(PropertyEnum.Adapter.class)
  public enum PropertyEnum {
    @SerializedName("USERNAME")
    USERNAME("USERNAME"),
    @SerializedName("ISSUER_DN")
    ISSUER_DN("ISSUER_DN"),
    @SerializedName("SUBJECT_DN")
    SUBJECT_DN("SUBJECT_DN"),
    @SerializedName("EXTERNAL_ACCOUNT_BINDING_ID")
    EXTERNAL_ACCOUNT_BINDING_ID("EXTERNAL_ACCOUNT_BINDING_ID"),
    @SerializedName("END_ENTITY_PROFILE")
    END_ENTITY_PROFILE("END_ENTITY_PROFILE"),
    @SerializedName("CERTIFICATE_PROFILE")
    CERTIFICATE_PROFILE("CERTIFICATE_PROFILE"),
    @SerializedName("STATUS")
    STATUS("STATUS"),
    @SerializedName("TAG")
    TAG("TAG"),
    @SerializedName("TYPE")
    TYPE("TYPE"),
    @SerializedName("UPDATE_TIME")
    UPDATE_TIME("UPDATE_TIME"),
    @SerializedName("ISSUED_DATE")
    ISSUED_DATE("ISSUED_DATE"),
    @SerializedName("EXPIRE_DATE")
    EXPIRE_DATE("EXPIRE_DATE"),
    @SerializedName("REVOCATION_DATE")
    REVOCATION_DATE("REVOCATION_DATE");

    private String value;

    PropertyEnum(String value) {
      this.value = value;
    }
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
    public static PropertyEnum fromValue(String input) {
      for (PropertyEnum b : PropertyEnum.values()) {
        if (b.value.equals(input)) {
          return b;
        }
      }
      return null;
    }
    public static class Adapter extends TypeAdapter<PropertyEnum> {
      @Override
      public void write(final JsonWriter jsonWriter, final PropertyEnum enumeration) throws IOException {
        jsonWriter.value(String.valueOf(enumeration.getValue()));
      }

      @Override
      public PropertyEnum read(final JsonReader jsonReader) throws IOException {
        Object value = jsonReader.nextString();
        return PropertyEnum.fromValue((String)(value));
      }
    }
  }  @SerializedName("property")
  private PropertyEnum property = null;

  /**
   * Sort ascending or descending. &#x27;ASC&#x27; for ascending, &#x27;DESC&#x27; for descending.
   */
  @JsonAdapter(OperationEnum.Adapter.class)
  public enum OperationEnum {
    @SerializedName("ASC")
    ASC("ASC"),
    @SerializedName("DESC")
    DESC("DESC");

    private String value;

    OperationEnum(String value) {
      this.value = value;
    }
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
    public static OperationEnum fromValue(String input) {
      for (OperationEnum b : OperationEnum.values()) {
        if (b.value.equals(input)) {
          return b;
        }
      }
      return null;
    }
    public static class Adapter extends TypeAdapter<OperationEnum> {
      @Override
      public void write(final JsonWriter jsonWriter, final OperationEnum enumeration) throws IOException {
        jsonWriter.value(String.valueOf(enumeration.getValue()));
      }

      @Override
      public OperationEnum read(final JsonReader jsonReader) throws IOException {
        Object value = jsonReader.nextString();
        return OperationEnum.fromValue((String)(value));
      }
    }
  }  @SerializedName("operation")
  private OperationEnum operation = null;

  public SearchCertificateSortRestRequest property(PropertyEnum property) {
    this.property = property;
    return this;
  }

   /**
   * Sorted by
   * @return property
  **/
  @Schema(description = "Sorted by")
  public PropertyEnum getProperty() {
    return property;
  }

  public void setProperty(PropertyEnum property) {
    this.property = property;
  }

  public SearchCertificateSortRestRequest operation(OperationEnum operation) {
    this.operation = operation;
    return this;
  }

   /**
   * Sort ascending or descending. &#x27;ASC&#x27; for ascending, &#x27;DESC&#x27; for descending.
   * @return operation
  **/
  @Schema(description = "Sort ascending or descending. 'ASC' for ascending, 'DESC' for descending.")
  public OperationEnum getOperation() {
    return operation;
  }

  public void setOperation(OperationEnum operation) {
    this.operation = operation;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SearchCertificateSortRestRequest searchCertificateSortRestRequest = (SearchCertificateSortRestRequest) o;
    return Objects.equals(this.property, searchCertificateSortRestRequest.property) &&
        Objects.equals(this.operation, searchCertificateSortRestRequest.operation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(property, operation);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SearchCertificateSortRestRequest {\n");

    sb.append("    property: ").append(toIndentedString(property)).append("\n");
    sb.append("    operation: ").append(toIndentedString(operation)).append("\n");
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
