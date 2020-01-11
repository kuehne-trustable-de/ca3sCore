import { IAcmeOrder } from '@/shared/model/acme-order.model';

export interface IIdentifier {
  id?: number;
  identifierId?: number;
  type?: string;
  value?: string;
  order?: IAcmeOrder;
}

export class Identifier implements IIdentifier {
  constructor(public id?: number, public identifierId?: number, public type?: string, public value?: string, public order?: IAcmeOrder) {}
}
