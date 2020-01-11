import { ICSR } from '@/shared/model/csr.model';
import { ICertificateAttribute } from '@/shared/model/certificate-attribute.model';
import { ICertificate } from '@/shared/model/certificate.model';

export interface ICertificate {
  id?: number;
  tbsDigest?: string;
  subject?: string;
  issuer?: string;
  type?: string;
  description?: string;
  subjectKeyIdentifier?: string;
  authorityKeyIdentifier?: string;
  fingerprint?: string;
  serial?: string;
  validFrom?: Date;
  validTo?: Date;
  creationExecutionId?: string;
  contentAddedAt?: Date;
  revokedSince?: Date;
  revocationReason?: string;
  revoked?: boolean;
  revocationExecutionId?: string;
  content?: any;
  csr?: ICSR;
  certificateAttributes?: ICertificateAttribute[];
  issuingCertificate?: ICertificate;
}

export class Certificate implements ICertificate {
  constructor(
    public id?: number,
    public tbsDigest?: string,
    public subject?: string,
    public issuer?: string,
    public type?: string,
    public description?: string,
    public subjectKeyIdentifier?: string,
    public authorityKeyIdentifier?: string,
    public fingerprint?: string,
    public serial?: string,
    public validFrom?: Date,
    public validTo?: Date,
    public creationExecutionId?: string,
    public contentAddedAt?: Date,
    public revokedSince?: Date,
    public revocationReason?: string,
    public revoked?: boolean,
    public revocationExecutionId?: string,
    public content?: any,
    public csr?: ICSR,
    public certificateAttributes?: ICertificateAttribute[],
    public issuingCertificate?: ICertificate
  ) {
    this.revoked = this.revoked || false;
  }
}
