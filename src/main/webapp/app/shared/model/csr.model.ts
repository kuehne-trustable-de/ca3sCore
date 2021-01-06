import { IRDN } from '@/shared/model/rdn.model';
import { IRequestAttribute } from '@/shared/model/request-attribute.model';
import { ICsrAttribute } from '@/shared/model/csr-attribute.model';
import { IPipeline } from '@/shared/model/pipeline.model';
import { ICertificate } from '@/shared/model/certificate.model';

export const enum PipelineType {
  ACME = 'ACME',
  SCEP = 'SCEP',
  WEB = 'WEB',
  INTERNAL = 'INTERNAL',
}

export const enum CsrStatus {
  PROCESSING = 'PROCESSING',
  ISSUED = 'ISSUED',
  REJECTED = 'REJECTED',
  PENDING = 'PENDING',
}

export interface ICSR {
  id?: number;
  csrBase64?: any;
  subject?: string;
  sans?: string;
  requestedOn?: Date;
  requestedBy?: string;
  pipelineType?: PipelineType;
  status?: CsrStatus;
  administeredBy?: string;
  approvedOn?: Date;
  rejectedOn?: Date;
  rejectionReason?: string;
  processInstanceId?: string;
  signingAlgorithm?: string;
  isCSRValid?: boolean;
  x509KeySpec?: string;
  publicKeyAlgorithm?: string;
  keyAlgorithm?: string;
  keyLength?: number;
  publicKeyHash?: string;
  serversideKeyGeneration?: boolean;
  subjectPublicKeyInfoBase64?: any;
  requestorComment?: any;
  administrationComment?: any;
  rdns?: IRDN[];
  ras?: IRequestAttribute[];
  csrAttributes?: ICsrAttribute[];
  pipeline?: IPipeline;
  certificate?: ICertificate;
}

export class CSR implements ICSR {
  constructor(
    public id?: number,
    public csrBase64?: any,
    public subject?: string,
    public sans?: string,
    public requestedOn?: Date,
    public requestedBy?: string,
    public pipelineType?: PipelineType,
    public status?: CsrStatus,
    public administeredBy?: string,
    public approvedOn?: Date,
    public rejectedOn?: Date,
    public rejectionReason?: string,
    public processInstanceId?: string,
    public signingAlgorithm?: string,
    public isCSRValid?: boolean,
    public x509KeySpec?: string,
    public publicKeyAlgorithm?: string,
    public keyAlgorithm?: string,
    public keyLength?: number,
    public publicKeyHash?: string,
    public serversideKeyGeneration?: boolean,
    public subjectPublicKeyInfoBase64?: any,
    public requestorComment?: any,
    public administrationComment?: any,
    public rdns?: IRDN[],
    public ras?: IRequestAttribute[],
    public csrAttributes?: ICsrAttribute[],
    public pipeline?: IPipeline,
    public certificate?: ICertificate
  ) {
    this.isCSRValid = this.isCSRValid || false;
    this.serversideKeyGeneration = this.serversideKeyGeneration || false;
  }
}
