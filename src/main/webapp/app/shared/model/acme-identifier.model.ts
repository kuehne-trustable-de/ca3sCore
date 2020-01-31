import { IAcmeOrder } from '@/shared/model/acme-order.model';

export interface IAcmeIdentifier {
  id?: number;
  acmeIdentifierId?: number;
  type?: string;
  value?: string;
  order?: IAcmeOrder;
}

export class AcmeIdentifier implements IAcmeIdentifier {
  constructor(
    public id?: number,
    public acmeIdentifierId?: number,
    public type?: string,
    public value?: string,
    public order?: IAcmeOrder
  ) {}
}
