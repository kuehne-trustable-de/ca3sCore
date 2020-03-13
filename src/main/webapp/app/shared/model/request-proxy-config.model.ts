import { IProtectedContent } from '@/shared/model/protected-content.model';

export interface IRequestProxyConfig {
  id?: number;
  name?: string;
  requestProxyUrl?: string;
  active?: boolean;
  secret?: IProtectedContent;
}

export class RequestProxyConfig implements IRequestProxyConfig {
  constructor(
    public id?: number,
    public name?: string,
    public requestProxyUrl?: string,
    public active?: boolean,
    public secret?: IProtectedContent
  ) {
    this.active = this.active || false;
  }
}
