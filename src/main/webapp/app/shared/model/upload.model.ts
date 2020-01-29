
export interface IUpload {
  user?: string;
  password?: string;
  csr?: string;
  checkResult?: string;
}

export class Upload implements IUpload {
  constructor(
    public user?: string,
    public password?: string,
    public csr?: string,
    public checkResult?: string
  ) {}
}
