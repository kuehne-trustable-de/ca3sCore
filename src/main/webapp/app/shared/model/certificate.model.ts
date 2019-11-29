import { Moment } from 'moment';
import { ICSR } from 'app/shared/model/csr.model';
import { ICertificateAttribute } from 'app/shared/model/certificate-attribute.model';
import { ICertificate } from 'app/shared/model/certificate.model';

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
  validFrom?: Moment;
  validTo?: Moment;
  creationExecutionId?: string;
  contentAddedAt?: Moment;
  revokedSince?: Moment;
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
    public validFrom?: Moment,
    public validTo?: Moment,
    public creationExecutionId?: string,
    public contentAddedAt?: Moment,
    public revokedSince?: Moment,
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
