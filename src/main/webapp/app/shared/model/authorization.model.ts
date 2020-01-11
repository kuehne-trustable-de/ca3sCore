import { IAcmeChallenge } from '@/shared/model/acme-challenge.model';
import { IAcmeOrder } from '@/shared/model/acme-order.model';

export interface IAuthorization {
  id?: number;
  authorizationId?: number;
  type?: string;
  value?: string;
  challenges?: IAcmeChallenge[];
  order?: IAcmeOrder;
}

export class Authorization implements IAuthorization {
  constructor(
    public id?: number,
    public authorizationId?: number,
    public type?: string,
    public value?: string,
    public challenges?: IAcmeChallenge[],
    public order?: IAcmeOrder
  ) {}
}
