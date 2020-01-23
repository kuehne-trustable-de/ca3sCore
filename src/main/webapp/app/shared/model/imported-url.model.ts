export interface IImportedURL {
  id?: number;
  name?: string;
  importDate?: Date;
}

export class ImportedURL implements IImportedURL {
  constructor(public id?: number, public name?: string, public importDate?: Date) {}
}
