
export interface IUpload {
  user?: string;
  password?: string;
  csr?: string;
}

export class Upload implements IUpload {
  constructor(
    public user?: string,
    public password?: string,
    public csr?: string
  ) {}
}
