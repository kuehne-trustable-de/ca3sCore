import { IRDN } from '@/shared/model/rdn.model';

export interface IRDNAttribute {
  id?: number;
  attributeType?: string;
  attributeValue?: string;
  rdn?: IRDN;
}

export class RDNAttribute implements IRDNAttribute {
  constructor(public id?: number, public attributeType?: string, public attributeValue?: string, public rdn?: IRDN) {}
}
