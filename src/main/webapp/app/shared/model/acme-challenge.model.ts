import { Moment } from 'moment';
import { IAuthorization } from 'app/shared/model/authorization.model';
import { ChallengeStatus } from 'app/shared/model/enumerations/challenge-status.model';

export interface IAcmeChallenge {
  id?: number;
  challengeId?: number;
  type?: string;
  value?: string;
  token?: string;
  validated?: Moment;
  status?: ChallengeStatus;
  authorization?: IAuthorization;
}

export class AcmeChallenge implements IAcmeChallenge {
  constructor(
    public id?: number,
    public challengeId?: number,
    public type?: string,
    public value?: string,
    public token?: string,
    public validated?: Moment,
    public status?: ChallengeStatus,
    public authorization?: IAuthorization
  ) {}
}
