import { IRequestAttribute } from 'app/shared/model/request-attribute.model';

export interface IRequestAttributeValue {
  id?: number;
  attributeValue?: string;
  reqAttr?: IRequestAttribute;
}

export class RequestAttributeValue implements IRequestAttributeValue {
  constructor(public id?: number, public attributeValue?: string, public reqAttr?: IRequestAttribute) {}
}
