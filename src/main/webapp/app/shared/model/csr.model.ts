import { IRDN } from '@/shared/model/rdn.model';
import { IRequestAttribute } from '@/shared/model/request-attribute.model';
import { ICsrAttribute } from '@/shared/model/csr-attribute.model';
import { IPipeline } from '@/shared/model/pipeline.model';
import { ICertificate } from '@/shared/model/certificate.model';

export const enum CsrStatus {
  PROCESSING = 'PROCESSING',
  ISSUED = 'ISSUED',
  REJECTED = 'REJECTED',
  PENDING = 'PENDING'
}

export interface ICSR {
  id?: number;
  csrBase64?: any;
  requestedOn?: Date;
  status?: CsrStatus;
  processInstanceId?: string;
  signingAlgorithm?: string;
  isCSRValid?: boolean;
  x509KeySpec?: string;
  publicKeyAlgorithm?: string;
  publicKeyHash?: string;
  subjectPublicKeyInfoBase64?: any;
  rdns?: IRDN[];
  ras?: IRequestAttribute[];
  csrAttributes?: ICsrAttribute[];
  pipelines?: IPipeline[];
  certificate?: ICertificate;
}

export class CSR implements ICSR {
  constructor(
    public id?: number,
    public csrBase64?: any,
    public requestedOn?: Date,
    public status?: CsrStatus,
    public processInstanceId?: string,
    public signingAlgorithm?: string,
    public isCSRValid?: boolean,
    public x509KeySpec?: string,
    public publicKeyAlgorithm?: string,
    public publicKeyHash?: string,
    public subjectPublicKeyInfoBase64?: any,
    public rdns?: IRDN[],
    public ras?: IRequestAttribute[],
    public csrAttributes?: ICsrAttribute[],
    public pipelines?: IPipeline[],
    public certificate?: ICertificate
  ) {
    this.isCSRValid = this.isCSRValid || false;
  }
}
