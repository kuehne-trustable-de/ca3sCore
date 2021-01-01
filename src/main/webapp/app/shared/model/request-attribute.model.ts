import { IRequestAttributeValue } from '@/shared/model/request-attribute-value.model';
import { ICSR } from '@/shared/model/csr.model';

export interface IRequestAttribute {
  id?: number;
  attributeType?: string;
  requestAttributeValues?: IRequestAttributeValue[];
  holdingRequestAttribute?: IRequestAttributeValue;
  csr?: ICSR;
}

export class RequestAttribute implements IRequestAttribute {
  constructor(
    public id?: number,
    public attributeType?: string,
    public requestAttributeValues?: IRequestAttributeValue[],
    public holdingRequestAttribute?: IRequestAttributeValue,
    public csr?: ICSR
  ) {}
}
