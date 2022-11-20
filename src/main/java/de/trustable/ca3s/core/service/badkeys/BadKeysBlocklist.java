package de.trustable.ca3s.core.service.badkeys;

import com.google.gson.JsonObject;

/*
      "blocklist":{
         "detected":true,
         "subtest":"debianssl",
         "blid":1,
         "lookup":"31c152529eb033a0",
         "debug":"Truncated Hash: 31c152529eb033a010f8c272fd8d42"
      }
 */

public class BadKeysBlocklist extends BadKeysResultInvalid {

    private int blid;
    private String lookup;
    private String debug;

    public BadKeysBlocklist(JsonObject jsonObject){

        super(jsonObject);

        if( jsonObject.has("blid")){
            blid = jsonObject.getAsJsonPrimitive("blid").getAsInt();
        }

        if( jsonObject.has("lookup")){
            lookup = jsonObject.getAsJsonPrimitive("lookup").getAsString();
        }

        if( jsonObject.has("debug")){
            debug = jsonObject.getAsJsonPrimitive("debug").getAsString();
        }

    }

    public int getBlid() {
        return blid;
    }

    public void setBlid(int blid) {
        this.blid = blid;
    }

    public String getLookup() {
        return lookup;
    }

    public void setLookup(String lookup) {
        this.lookup = lookup;
    }

    public String getDebug() {
        return debug;
    }

    public void setDebug(String debug) {
        this.debug = debug;
    }
}
