package de.trustable.ca3s.core.service.badkeys;

import com.google.gson.JsonObject;

import java.io.Serializable;
import java.math.BigInteger;

public class BadKeysResultFermat implements Serializable {

    private BigInteger p;
    private BigInteger q;
    private BigInteger a;
    private BigInteger b;
    private String debug;

    public BadKeysResultFermat(JsonObject jsonObject){

        if( jsonObject.has("p")){
            p = jsonObject.getAsJsonPrimitive("p").getAsBigInteger();
        }

        if( jsonObject.has("q")){
            q = jsonObject.getAsJsonPrimitive("q").getAsBigInteger();
        }

        if( jsonObject.has("a")){
            a = jsonObject.getAsJsonPrimitive("a").getAsBigInteger();
        }

        if( jsonObject.has("b")){
            b = jsonObject.getAsJsonPrimitive("b").getAsBigInteger();
        }

        if( jsonObject.has("debug")){
            debug = jsonObject.getAsJsonPrimitive("debug").getAsString();
        }

    }
    public BigInteger getP() {
        return p;
    }

    public void setP(BigInteger p) {
        this.p = p;
    }

    public BigInteger getQ() {
        return q;
    }

    public void setQ(BigInteger q) {
        this.q = q;
    }

    public BigInteger getA() {
        return a;
    }

    public void setA(BigInteger a) {
        this.a = a;
    }

    public BigInteger getB() {
        return b;
    }

    public void setB(BigInteger b) {
        this.b = b;
    }

    public String getDebug() {
        return debug;
    }

    public void setDebug(String debug) {
        this.debug = debug;
    }
}
