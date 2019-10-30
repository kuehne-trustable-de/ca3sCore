export interface ISelectorToTemplate {
  id?: number;
  selector?: string;
  template?: string;
  comment?: string;
}

export class SelectorToTemplate implements ISelectorToTemplate {
  constructor(public id?: number, public selector?: string, public template?: string, public comment?: string) {}
}
