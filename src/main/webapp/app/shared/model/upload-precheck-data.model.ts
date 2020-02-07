
export interface IUploadPrecheckData {
  user?: string;
  password?: string;
  csr?: string;
}

export class UploadPrecheckData implements IUploadPrecheckData {
  constructor(
    public user?: string,
    public password?: string,
    public csr?: string
  ) {}
}
