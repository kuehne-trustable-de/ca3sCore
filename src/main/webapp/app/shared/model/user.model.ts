export interface IUser {
  id?: any;
  login?: string;
  firstName?: string;
  lastName?: string;
  email?: string;
  phone?: string;
  activated?: boolean;
  langKey?: string;
  authorities?: any[];
  createdBy?: string;
  createdDate?: Date;
  lastModifiedBy?: string;
  lastModifiedDate?: Date;
  password?: string;

  tenantId?: any;
  tenantName?: string;

  failedLogins?: number;
  blockedUntilDate?: Date;
  credentialsValidToDate?: Date;
  managedExternally?: boolean;
  blocked?: boolean;
}

export class User implements IUser {
  constructor(
    public id?: any,
    public login?: string,
    public firstName?: string,
    public lastName?: string,
    public email?: string,
    public phone?: string,
    public activated?: boolean,
    public langKey?: string,
    public authorities?: any[],
    public createdBy?: string,
    public createdDate?: Date,
    public lastModifiedBy?: string,
    public lastModifiedDate?: Date,
    public password?: string,
    public tenantId?: any,
    public tenantName?: string,
    public failedLogins?: number,
    public blockedUntilDate?: Date,
    public credentialsValidToDate?: Date,
    public managedExternally?: boolean,
    public blocked?: boolean
  ) {}
}
