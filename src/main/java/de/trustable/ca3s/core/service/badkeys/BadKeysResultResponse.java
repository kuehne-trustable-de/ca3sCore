package de.trustable.ca3s.core.service.badkeys;

import com.google.gson.JsonObject;

import java.io.Serializable;
import java.math.BigInteger;

/*
{
   "type":"rsa",
   "n":24311882281058668049780886014595327041639234369946369425953519224297403604833822828870284828105889019891909152560682812604138745216386881538139876632181010302159492345797403908806899068984985769303841260310519239895605159812461111159308300818347859040344163986366195515743720923355160895207219727180388828851979701042126498240486022105485370844380117420575752792229950380055925943879791054620110370887532408825024997640751783126178093315788663534933563387734354344302477503367853332341100260355478215318909687686372227093153435085802166380043778644025584592539536707086134503979421589196595639518504129374336051291481,
   "e":65537,
   "bits":2048,
   "results":{
      "blocklist":{
         "detected":true,
         "subtest":"debianssl",
         "blid":1,
         "lookup":"31c152529eb033a0",
         "debug":"Truncated Hash: 31c152529eb033a010f8c272fd8d42"
      }
   },
   "spkisha256":"9449eb4873a6a19a803682bcadbdca86e8c54b139e6105c1f9dc00e7b2b997a2"
 */
public class BadKeysResultResponse implements Serializable {
    private String type;
    private BigInteger n;
    private BigInteger e;
    private BigInteger x;
    private BigInteger y;
    private long bits;
    private String spkisha256;
    private BadKeysResultDetails results;

    public BadKeysResultResponse(JsonObject jsonObject){

        if( jsonObject.has("type")){
            type = jsonObject.getAsJsonPrimitive("type").getAsString();
        }

        if( jsonObject.has("results")){
            results = new BadKeysResultDetails(jsonObject.getAsJsonObject("results"));
        }

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigInteger getN() {
        return n;
    }

    public void setN(BigInteger n) {
        this.n = n;
    }

    public BigInteger getE() {
        return e;
    }

    public void setE(BigInteger e) {
        this.e = e;
    }

    public BigInteger getX() {
        return x;
    }

    public void setX(BigInteger x) {
        this.x = x;
    }

    public BigInteger getY() {
        return y;
    }

    public void setY(BigInteger y) {
        this.y = y;
    }

    public long getBits() {
        return bits;
    }

    public void setBits(long bits) {
        this.bits = bits;
    }

    public String getSpkisha256() {
        return spkisha256;
    }

    public void setSpkisha256(String spkisha256) {
        this.spkisha256 = spkisha256;
    }

    public BadKeysResultDetails getResults() {
        return results;
    }

    public void setResults(BadKeysResultDetails results) {
        this.results = results;
    }
}
