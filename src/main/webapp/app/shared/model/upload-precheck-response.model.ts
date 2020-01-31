
export interface IUploadPrecheckResponse {
  type?: string;
  checkResult?: string;
}

export class UploadPrecheckResponse implements IUploadPrecheckResponse {
  constructor(
    public type?: string,
    public checkResult?: string
  ) {}
}
