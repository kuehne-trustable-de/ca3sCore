import { IAuthorization } from '@/shared/model/authorization.model';
import { IIdentifier } from '@/shared/model/identifier.model';
import { ICSR } from '@/shared/model/csr.model';
import { ICertificate } from '@/shared/model/certificate.model';
import { IACMEAccount } from '@/shared/model/acme-account.model';

export const enum OrderStatus {
  PENDING = 'PENDING',
  READY = 'READY',
  PROCESSING = 'PROCESSING',
  VALID = 'VALID',
  INVALID = 'INVALID'
}

export interface IAcmeOrder {
  id?: number;
  orderId?: number;
  status?: OrderStatus;
  expires?: Date;
  notBefore?: Date;
  notAfter?: Date;
  error?: string;
  finalizeUrl?: string;
  certificateUrl?: string;
  authorizations?: IAuthorization[];
  identifiers?: IIdentifier[];
  csr?: ICSR;
  certificate?: ICertificate;
  account?: IACMEAccount;
}

export class AcmeOrder implements IAcmeOrder {
  constructor(
    public id?: number,
    public orderId?: number,
    public status?: OrderStatus,
    public expires?: Date,
    public notBefore?: Date,
    public notAfter?: Date,
    public error?: string,
    public finalizeUrl?: string,
    public certificateUrl?: string,
    public authorizations?: IAuthorization[],
    public identifiers?: IIdentifier[],
    public csr?: ICSR,
    public certificate?: ICertificate,
    public account?: IACMEAccount
  ) {}
}
