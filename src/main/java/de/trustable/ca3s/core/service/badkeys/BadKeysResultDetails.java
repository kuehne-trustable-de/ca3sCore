package de.trustable.ca3s.core.service.badkeys;

import com.google.gson.JsonObject;

import java.io.Serializable;

/*
      "blocklist":{
         "detected":true,
         "subtest":"debianssl",
         "blid":1,
         "lookup":"31c152529eb033a0",
         "debug":"Truncated Hash: 31c152529eb033a010f8c272fd8d42"
      }
 */
public class BadKeysResultDetails implements Serializable {

    private BadKeysBlocklist blocklist;
    private BadKeysResultInvalid rsaInvalid;
    private BadKeysResultInvalid roca;
    private BadKeysResultInvalid pattern;
    private BadKeysResultFermat fermat;
    private String resultType = "valid";

    public BadKeysResultDetails(JsonObject jsonObject){

        if( jsonObject.has("blocklist")){
            blocklist = new BadKeysBlocklist(jsonObject.getAsJsonObject("blocklist"));
            resultType = "blocklist";
        }
        if( jsonObject.has("rsainvalid")){
            rsaInvalid = new BadKeysResultInvalid(jsonObject.getAsJsonObject("rsainvalid"));
            resultType = "rsainvalid";
        }
        if( jsonObject.has("roca")){
            roca = new BadKeysResultInvalid(jsonObject.getAsJsonObject("roca"));
            resultType = "roca";
        }
        if( jsonObject.has("pattern")){
            pattern = new BadKeysResultInvalid(jsonObject.getAsJsonObject("pattern"));
            resultType = "pattern";
        }

        if( jsonObject.has("fermat")){
            fermat = new BadKeysResultFermat(jsonObject.getAsJsonObject("fermat"));
            resultType = "fermat";
        }

    }


    public BadKeysBlocklist getBlocklist() {
        return blocklist;
    }

    public void setBlocklist(BadKeysBlocklist blocklist) {
        this.blocklist = blocklist;
    }

    public BadKeysResultInvalid getRsaInvalid() {
        return rsaInvalid;
    }

    public void setRsaInvalid(BadKeysResultInvalid rsaInvalid) {
        this.rsaInvalid = rsaInvalid;
    }

    public BadKeysResultInvalid getRoca() {
        return roca;
    }

    public void setRoca(BadKeysResultInvalid roca) {
        this.roca = roca;
    }

    public BadKeysResultInvalid getPattern() {
        return pattern;
    }

    public void setPattern(BadKeysResultInvalid pattern) {
        this.pattern = pattern;
    }

    public BadKeysResultFermat getFermat() {
        return fermat;
    }

    public void setFermat(BadKeysResultFermat fermat) {
        this.fermat = fermat;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }
}
