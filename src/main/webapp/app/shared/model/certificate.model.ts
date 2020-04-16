import { ICSR } from '@/shared/model/csr.model';
import { ICertificateAttribute } from '@/shared/model/certificate-attribute.model';
import { ICertificate } from '@/shared/model/certificate.model';

export interface ICertificate {
  id?: number;
  tbsDigest?: string;
  subject?: string;
  sans?: string;
  issuer?: string;
  root?: string;
  type?: string;
  description?: string;
  fingerprint?: string;
  serial?: string;
  validFrom?: Date;
  validTo?: Date;
  keyAlgorithm?: string;
  keyLength?: number;
  curveName?: string;
  hashingAlgorithm?: string;
  paddingAlgorithm?: string;
  signingAlgorithm?: string;
  creationExecutionId?: string;
  contentAddedAt?: Date;
  revokedSince?: Date;
  revocationReason?: string;
  revoked?: boolean;
  revocationExecutionId?: string;
  administrationComment?: any;
  endEntity?: boolean;
  selfsigned?: boolean;
  content?: any;
  csr?: ICSR;
  certificateAttributes?: ICertificateAttribute[];
  issuingCertificate?: ICertificate;
  rootCertificate?: ICertificate;
}

export class Certificate implements ICertificate {
  constructor(
    public id?: number,
    public tbsDigest?: string,
    public subject?: string,
    public sans?: string,
    public issuer?: string,
    public root?: string,
    public type?: string,
    public description?: string,
    public fingerprint?: string,
    public serial?: string,
    public validFrom?: Date,
    public validTo?: Date,
    public keyAlgorithm?: string,
    public keyLength?: number,
    public curveName?: string,
    public hashingAlgorithm?: string,
    public paddingAlgorithm?: string,
    public signingAlgorithm?: string,
    public creationExecutionId?: string,
    public contentAddedAt?: Date,
    public revokedSince?: Date,
    public revocationReason?: string,
    public revoked?: boolean,
    public revocationExecutionId?: string,
    public administrationComment?: any,
    public endEntity?: boolean,
    public selfsigned?: boolean,
    public content?: any,
    public csr?: ICSR,
    public certificateAttributes?: ICertificateAttribute[],
    public issuingCertificate?: ICertificate,
    public rootCertificate?: ICertificate
  ) {
    this.revoked = this.revoked || false;
    this.endEntity = this.endEntity || false;
    this.selfsigned = this.selfsigned || false;
  }
}
