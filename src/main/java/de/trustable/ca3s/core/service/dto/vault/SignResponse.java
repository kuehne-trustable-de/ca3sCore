package de.trustable.ca3s.core.service.dto.vault;

import java.io.Serializable;

/**
 *     {
 *         "lease_id": "pki/issue/test/7ad6cfa5-f04f-c62a-d477-f33210475d05",
 *         "renewable": false,
 *         "lease_duration": 21600,
 *
 *         "data": {
 *         "certificate": "-----BEGIN CERTIFICATE-----\nMIIDzDCCAragAwIBAgIUOd0ukLcjH43TfTHFG9qE0FtlMVgwCwYJKoZIhvcNAQEL\n...\numkqeYeO30g1uYvDuWLXVA==\n-----END CERTIFICATE-----\n",
 *             "issuing_ca": "-----BEGIN CERTIFICATE-----\nMIIDUTCCAjmgAwIBAgIJAKM+z4MSfw2mMA0GCSqGSIb3DQEBCwUAMBsxGTAXBgNV\n...\nG/7g4koczXLoUM3OQXd5Aq2cs4SS1vODrYmgbioFsQ3eDHd1fg==\n-----END CERTIFICATE-----\n",
 *             "ca_chain": [
 *         "-----BEGIN CERTIFICATE-----\nMIIDUTCCAjmgAwIBAgIJAKM+z4MSfw2mMA0GCSqGSIb3DQEBCwUAMBsxGTAXBgNV\n...\nG/7g4koczXLoUM3OQXd5Aq2cs4SS1vODrYmgbioFsQ3eDHd1fg==\n-----END CERTIFICATE-----\n"
 *     ],
 *         "private_key": "-----BEGIN RSA PRIVATE KEY-----\nMIIEowIBAAKCAQEAnVHfwoKsUG1GDVyWB1AFroaKl2ImMBO8EnvGLRrmobIkQvh+\n...\nQN351pgTphi6nlCkGPzkDuwvtxSxiCWXQcaxrHAL7MiJpPzkIBq1\n-----END RSA PRIVATE KEY-----\n",
 *             "private_key_type": "rsa",
 *             "serial_number": "39:dd:2e:90:b7:23:1f:8d:d3:7d:31:c5:1b:da:84:d0:5b:65:31:58"
 *     },
 *         "warnings": "",
 *         "auth": null
 *     }
 */
public class SignResponse implements Serializable {


    private String lease_id;
    private boolean renewable;
    private long lease_duration;

    private CertificateAndKey data;
    private String warnings;
    private String auth;

    public SignResponse() {
    }

    public String getLease_id() {
        return lease_id;
    }

    public void setLease_id(String lease_id) {
        this.lease_id = lease_id;
    }

    public boolean isRenewable() {
        return renewable;
    }

    public void setRenewable(boolean renewable) {
        this.renewable = renewable;
    }

    public long getLease_duration() {
        return lease_duration;
    }

    public void setLease_duration(long lease_duration) {
        this.lease_duration = lease_duration;
    }

    public CertificateAndKey getData() {
        return data;
    }

    public void setData(CertificateAndKey data) {
        this.data = data;
    }

    public String getWarnings() {
        return warnings;
    }

    public void setWarnings(String warnings) {
        this.warnings = warnings;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    @Override
    public String toString() {
        return "SignResponse{" +
            "lease_id='" + lease_id + '\'' +
            ", renewable=" + renewable +
            ", lease_duration=" + lease_duration +
            ", certificateAndKey=" + data +
            ", warnings='" + warnings + '\'' +
            ", auth='" + auth + '\'' +
            '}';
    }
}
