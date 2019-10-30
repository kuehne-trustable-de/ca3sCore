import { Moment } from 'moment';
import { IRDN } from 'app/shared/model/rdn.model';
import { IRequestAttribute } from 'app/shared/model/request-attribute.model';
import { ICsrAttribute } from 'app/shared/model/csr-attribute.model';

export interface ICSR {
  id?: number;
  csrBase64?: any;
  requestedOn?: Moment;
  status?: string;
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
}

export class CSR implements ICSR {
  constructor(
    public id?: number,
    public csrBase64?: any,
    public requestedOn?: Moment,
    public status?: string,
    public processInstanceId?: string,
    public signingAlgorithm?: string,
    public isCSRValid?: boolean,
    public x509KeySpec?: string,
    public publicKeyAlgorithm?: string,
    public publicKeyHash?: string,
    public subjectPublicKeyInfoBase64?: any,
    public rdns?: IRDN[],
    public ras?: IRequestAttribute[],
    public csrAttributes?: ICsrAttribute[]
  ) {
    this.isCSRValid = this.isCSRValid || false;
  }
}
