/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 2.19.577 on 2020-03-02 23:19:47.

export interface ICertificateView extends ISerializable {
    id?: number;
    tbsDigest?: string;
    subject?: string;
    issuer?: string;
    type?: string;
    keyLength?: string;
    signingAlgorithm?: string;
    paddingAlgorithm?: string;
    hashAlgorithm?: string;
    description?: string;
    serial?: string;
    validFrom?: Date;
    validTo?: Date;
    contentAddedAt?: Date;
    revokedSince?: Date;
    revocationReason?: string;
    revoked?: boolean;
}

export interface IUploadPrecheckData {
    passphrase?: string;
    content?: string;
}

export interface IX509CertificateHolderShallow {
    subject?: string;
    issuer?: string;
    type?: string;
    fingerprint?: string;
    serial?: string;
    validFrom?: Date;
    validTo?: Date;
    extensions?: string[];
    keyPresent?: boolean;
    pemCertrificate?: string;
    sans?: string[];
}

export interface ICertificateFilter extends ISerializable {
    attributeName?: string;
    attributeValue?: string;
    selector?: ISelector;
}

export interface ICertificateSelectionData extends ISerializable {
    itemName?: string;
    itemType?: string;
    itemDefaultSelector?: ISelector;
    itemDefaultValue?: string;
}

export interface IPkcsXXData {
    dataType?: IPKCSDataType;
    p10Holder?: IPkcs10RequestHolderShallow;
    certificates?: IX509CertificateHolderShallow[];
    certificatePresentInDB?: boolean;
    publicKeyPresentInDB?: boolean;
    certificateId?: number;
}

export interface ISerializable {
}

export interface IPkcs10RequestHolderShallow {
    csrvalid?: boolean;
    signingAlgorithmName?: string;
    isCSRValid?: boolean;
    x509KeySpec?: string;
    sans?: string[];
    subject?: string;
    publicKeyAlgorithmName?: string;
}

export type ISelector = "EQUALS" | "LIKE" | "NOTLIKE" | "LESSTHAN" | "GREATERTHAN" | "ON" | "BEFORE" | "AFTER" | "ISTRUE" | "ISFALSE";

export type IPKCSDataType = "CSR" | "X509_CERTIFICATE" | "UNKNOWN";
