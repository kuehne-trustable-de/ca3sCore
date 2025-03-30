package de.trustable.ca3s.core.web.rest.data;

import java.io.Serializable;

public class OTPDetailsResponse implements Serializable {
    private String seed;
    private String totpUrl;
    private byte[] qrCodeImg;

    public OTPDetailsResponse(){}

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

    public String getTotpUrl() {
        return totpUrl;
    }

    public void setTotpUrl(String totpUrl) {
        this.totpUrl = totpUrl;
    }

    public byte[] getQrCodeImg() {
        return qrCodeImg;
    }

    public void setQrCodeImg(byte[] qrCodeImg) {
        this.qrCodeImg = qrCodeImg;
    }
}
