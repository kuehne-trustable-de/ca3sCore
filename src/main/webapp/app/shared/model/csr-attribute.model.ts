import { ICSR } from 'app/shared/model/csr.model';

export interface ICsrAttribute {
  id?: number;
  name?: string;
  value?: string;
  csr?: ICSR;
}

export class CsrAttribute implements ICsrAttribute {
  constructor(public id?: number, public name?: string, public value?: string, public csr?: ICSR) {}
}
