import { ICSR } from '@/shared/model/csr.model';

export interface ICsrAttribute {
  id?: number;
  name?: string;
  value?: any;
  csr?: ICSR;
}

export class CsrAttribute implements ICsrAttribute {
  constructor(public id?: number, public name?: string, public value?: any, public csr?: ICSR) {}
}
