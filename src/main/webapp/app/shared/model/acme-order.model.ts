import { Moment } from 'moment';
import { IAuthorization } from 'app/shared/model/authorization.model';
import { IIdentifier } from 'app/shared/model/identifier.model';
import { ICSR } from 'app/shared/model/csr.model';
import { ICertificate } from 'app/shared/model/certificate.model';
import { IACMEAccount } from 'app/shared/model/acme-account.model';
import { OrderStatus } from 'app/shared/model/enumerations/order-status.model';

export interface IAcmeOrder {
  id?: number;
  orderId?: number;
  status?: OrderStatus;
  expires?: Moment;
  notBefore?: Moment;
  notAfter?: Moment;
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
    public expires?: Moment,
    public notBefore?: Moment,
    public notAfter?: Moment,
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
