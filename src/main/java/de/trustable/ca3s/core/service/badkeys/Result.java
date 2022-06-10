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
public class Result implements Serializable {

    private Blocklist blocklist;
    private Invalid rsaInvalid;
    private Invalid roca;
    private Invalid pattern;
    private Fermat fermat;
    private String resultType = "valid";

    public Result(JsonObject jsonObject){

        if( jsonObject.has("blocklist")){
            blocklist = new Blocklist(jsonObject.getAsJsonObject("blocklist"));
            resultType = "blocklist";
        }
        if( jsonObject.has("rsainvalid")){
            rsaInvalid = new Invalid(jsonObject.getAsJsonObject("rsainvalid"));
            resultType = "rsainvalid";
        }
        if( jsonObject.has("roca")){
            roca = new Invalid(jsonObject.getAsJsonObject("roca"));
            resultType = "roca";
        }
        if( jsonObject.has("pattern")){
            pattern = new Invalid(jsonObject.getAsJsonObject("pattern"));
            resultType = "pattern";
        }

        if( jsonObject.has("fermat")){
            fermat = new Fermat(jsonObject.getAsJsonObject("fermat"));
            resultType = "fermat";
        }

    }


    public Blocklist getBlocklist() {
        return blocklist;
    }

    public void setBlocklist(Blocklist blocklist) {
        this.blocklist = blocklist;
    }

    public Invalid getRsaInvalid() {
        return rsaInvalid;
    }

    public void setRsaInvalid(Invalid rsaInvalid) {
        this.rsaInvalid = rsaInvalid;
    }

    public Invalid getRoca() {
        return roca;
    }

    public void setRoca(Invalid roca) {
        this.roca = roca;
    }

    public Invalid getPattern() {
        return pattern;
    }

    public void setPattern(Invalid pattern) {
        this.pattern = pattern;
    }

    public Fermat getFermat() {
        return fermat;
    }

    public void setFermat(Fermat fermat) {
        this.fermat = fermat;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }
}
