import { IAcmeChallenge } from 'app/shared/model/acme-challenge.model';
import { IAcmeOrder } from 'app/shared/model/acme-order.model';

export interface IAuthorization {
  id?: number;
  type?: string;
  value?: string;
  challenges?: IAcmeChallenge[];
  order?: IAcmeOrder;
}

export class Authorization implements IAuthorization {
  constructor(
    public id?: number,
    public type?: string,
    public value?: string,
    public challenges?: IAcmeChallenge[],
    public order?: IAcmeOrder
  ) {}
}
