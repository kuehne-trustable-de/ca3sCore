package de.trustable.ca3s.core.service.dto.vault;

import java.io.Serializable;

public class SignRequest implements Serializable {

    private String csr;
    private String common_name;
    private String alt_names;
    private String other_sans;
    private String ip_sans;
    private String uri_sans;
    private String ttl;
    private String format;

    private boolean exclude_cn_from_sans;
    private String not_after;
    private boolean remove_roots_from_chain;
    private String user_ids;

    public SignRequest(){}

    public String getCsr() {
        return csr;
    }

    public void setCsr(String csr) {
        this.csr = csr;
    }

    public String getCommon_name() {
        return common_name;
    }

    public void setCommon_name(String common_name) {
        this.common_name = common_name;
    }

    public String getAlt_names() {
        return alt_names;
    }

    public void setAlt_names(String alt_names) {
        this.alt_names = alt_names;
    }

    public String getOther_sans() {
        return other_sans;
    }

    public void setOther_sans(String other_sans) {
        this.other_sans = other_sans;
    }

    public String getIp_sans() {
        return ip_sans;
    }

    public void setIp_sans(String ip_sans) {
        this.ip_sans = ip_sans;
    }

    public String getUri_sans() {
        return uri_sans;
    }

    public void setUri_sans(String uri_sans) {
        this.uri_sans = uri_sans;
    }

    public String getTtl() {
        return ttl;
    }

    public void setTtl(String ttl) {
        this.ttl = ttl;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public boolean isExclude_cn_from_sans() {
        return exclude_cn_from_sans;
    }

    public void setExclude_cn_from_sans(boolean exclude_cn_from_sans) {
        this.exclude_cn_from_sans = exclude_cn_from_sans;
    }

    public String getNot_after() {
        return not_after;
    }

    public void setNot_after(String not_after) {
        this.not_after = not_after;
    }

    public boolean isRemove_roots_from_chain() {
        return remove_roots_from_chain;
    }

    public void setRemove_roots_from_chain(boolean remove_roots_from_chain) {
        this.remove_roots_from_chain = remove_roots_from_chain;
    }

    public String getUser_ids() {
        return user_ids;
    }

    public void setUser_ids(String user_ids) {
        this.user_ids = user_ids;
    }

    @Override
    public String toString() {
        return "SignRequest{" +
            "csr='" + csr + '\'' +
            ", common_name='" + common_name + '\'' +
            ", alt_names='" + alt_names + '\'' +
            ", other_sans='" + other_sans + '\'' +
            ", ip_sans='" + ip_sans + '\'' +
            ", uri_sans='" + uri_sans + '\'' +
            ", ttl='" + ttl + '\'' +
            ", format='" + format + '\'' +
            ", exclude_cn_from_sans=" + exclude_cn_from_sans +
            ", not_after='" + not_after + '\'' +
            ", remove_roots_from_chain='" + remove_roots_from_chain + '\'' +
            ", user_ids='" + user_ids + '\'' +
            '}';
    }
}
