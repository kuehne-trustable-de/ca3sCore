
export interface IUploadPrecheckResponse {
  dataType?: string;
  checkResult?: string;
}

export class UploadPrecheckResponse implements IUploadPrecheckResponse {
  constructor(
    public dataType?: string,
    public checkResult?: string
  ) {}
}
