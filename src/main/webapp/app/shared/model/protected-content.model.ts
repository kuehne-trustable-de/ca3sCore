export const enum ProtectedContentType {
  KEY = 'KEY',
  SECRET = 'SECRET',
  PASSWORD = 'PASSWORD'
}

export const enum ContentRelationType {
  CERTIFICATE = 'CERTIFICATE',
  CONNECTION = 'CONNECTION'
}

export interface IProtectedContent {
  id?: number;
  contentBase64?: any;
  type?: ProtectedContentType;
  relationType?: ContentRelationType;
  relatedId?: number;
}

export class ProtectedContent implements IProtectedContent {
  constructor(
    public id?: number,
    public contentBase64?: any,
    public type?: ProtectedContentType,
    public relationType?: ContentRelationType,
    public relatedId?: number
  ) {}
}
