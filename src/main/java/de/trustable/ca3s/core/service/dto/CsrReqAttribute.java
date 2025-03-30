package de.trustable.ca3s.core.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.trustable.util.OidNameMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class CsrReqAttribute implements Serializable {


    /**
     *
     */
    private static final long serialVersionUID = -47605986434456560L;

    private final Logger LOG = LoggerFactory.getLogger(CsrReqAttribute.class);


    @JsonProperty("oid")
    private String oid;

    @JsonProperty("oidName")
    private String oidName;

    @JsonProperty("value")
    private String value;


    public CsrReqAttribute(Map<String, Object> extMap){
        this.oid = (String)extMap.get("oid");
        this.oidName = OidNameMapper.lookupOid(oid);

        Object valueList = extMap.get("value");
        this.value = "";
        if( valueList instanceof List) {
            for (Object obj : (List) valueList) {
                if (!value.isEmpty()) {
                    this.value += ", ";
                }
                String val = obj.toString();
                String valueName = OidNameMapper.lookupOid(val);
                if( val.equals(valueName)){
                    this.value += val;
                }else{
                    this.value += valueName + " (" + val + ")";
                }
            }
        }else{
            this.value = valueList.toString();
        }
    }

    public String getOid() {
        return oid;
    }

    public String getOidName() {
        return oidName;
    }

    public String getValue() {
        return value;
    }
}
