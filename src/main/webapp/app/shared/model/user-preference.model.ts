export interface IUserPreference {
  id?: number;
  userId?: number;
  name?: string;
  content?: any;
}

export class UserPreference implements IUserPreference {
  constructor(public id?: number, public userId?: number, public name?: string, public content?: any) {}
}
