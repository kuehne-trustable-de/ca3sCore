/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 2.19.577 on 2020-04-20 22:18:16.

export interface ICertificateView extends ISerializable {
    id?: number;
    tbsDigest?: string;
    subject?: string;
    sans?: string;
    issuer?: string;
    type?: string;
    keyLength?: string;
    keyAlgorithm?: string;
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

export interface ICSRView extends ISerializable {
    id?: number;
    certificateId?: number;
    status?: ICsrStatus;
    subject?: string;
    sans?: string;
    pipelineType?: IPipelineType;
    rejectedOn?: Date;
    rejectionReason?: string;
    requestedBy?: string;
    processingCA?: string;
    pipelineName?: string;
    x509KeySpec?: string;
    keyLength?: string;
    signingAlgorithm?: string;
    publicKeyAlgorithm?: string;
    requestedOn?: Date;
}

export interface ICSRAdministrationData extends ISerializable {
    csrId?: number;
    administrationType?: IAdministrationType;
    rejectionReason?: string;
    comment?: string;
}

export interface ICertificateAdministrationData extends ISerializable {
    certificateId?: number;
    revocationReason?: string;
    comment?: string;
}

export interface IUploadPrecheckData {
    passphrase?: string;
    requestorcomment?: string;
    pipelineId?: number;
    content?: string;
    namedValues?: INamedValue[];
}

export interface IX509CertificateHolderShallow {
    certificateId?: number;
    subject?: string;
    issuer?: string;
    type?: string;
    fingerprint?: string;
    serial?: string;
    validFrom?: Date;
    validTo?: Date;
    extensions?: string[];
    keyPresent?: boolean;
    certificatePresentInDB?: boolean;
    publicKeyPresentInDB?: boolean;
    pemCertrificate?: string;
    sans?: string[];
}

export interface ICertificateFilter extends ISerializable {
    attributeName?: string;
    attributeValue?: string;
    selector?: ISelector;
}

export interface ICertificateFilterList extends ISerializable {
    filterList?: ICertificateFilter[];
}

export interface ICertificateSelectionData extends ISerializable {
    itemName?: string;
    itemType?: string;
    itemDefaultSelector?: ISelector;
    itemDefaultValue?: string;
    values?: string[];
}

export interface IPkcsXXData {
    csrPublicKeyPresentInDB?: boolean;
    dataType?: IPKCSDataType;
    p10Holder?: IPkcs10RequestHolderShallow;
    certificates?: IX509CertificateHolderShallow[];
    createdCertificateId?: string;
    passphraseRequired?: boolean;
    csrPending?: boolean;
    createdCSRId?: string;
}

export interface IDataCollection {
    labels?: string[];
    datasets?: IDataSet[];
}

export interface IDataSet {
    label?: string;
    data?: number[];
    backgroundColor?: string[];
}

export interface ISerializable {
}

export interface INamedValue {
    name?: string;
    value?: string;
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

export type ICAStatus = "Active" | "Deactivated" | "Unknown";

export type ISelector = "EQUAL" | "NOT_EQUAL" | "LIKE" | "NOTLIKE" | "LESSTHAN" | "GREATERTHAN" | "ON" | "BEFORE" | "AFTER" | "ISTRUE" | "ISFALSE";

export type ICsrStatus = "PROCESSING" | "ISSUED" | "REJECTED" | "PENDING";

export type IPipelineType = "ACME" | "SCEP" | "WEB" | "INTERNAL";

export type IAdministrationType = "ACCEPT" | "REJECT";

export type IPKCSDataType = "CSR" | "X509_CERTIFICATE" | "X509_CERTIFICATE_CREATED" | "UNKNOWN" | "CONTAINER" | "CONTAINER_REQUIRING_PASSPHRASE";
