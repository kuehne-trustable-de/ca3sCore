package de.trustable.ca3s.core.service.badkeys;

import com.google.gson.JsonObject;

import java.io.Serializable;

public class BadKeysResultInvalid implements Serializable {

    private boolean detected;
    private String subtest;

    public BadKeysResultInvalid(JsonObject jsonObject){

        if( jsonObject.has("detected")){
            detected = jsonObject.getAsJsonPrimitive("detected").getAsBoolean();
        }

        if( jsonObject.has("subtest")){
            subtest = jsonObject.getAsJsonPrimitive("subtest").getAsString();
        }

    }

    public boolean isDetected() {
        return detected;
    }

    public void setDetected(boolean detected) {
        this.detected = detected;
    }

    public String getSubtest() {
        return subtest;
    }

    public void setSubtest(String subtest) {
        this.subtest = subtest;
    }
}
