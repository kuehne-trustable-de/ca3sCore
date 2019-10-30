import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IIdentifier } from 'app/shared/model/identifier.model';

type EntityResponseType = HttpResponse<IIdentifier>;
type EntityArrayResponseType = HttpResponse<IIdentifier[]>;

@Injectable({ providedIn: 'root' })
export class IdentifierService {
  public resourceUrl = SERVER_API_URL + 'api/identifiers';

  constructor(protected http: HttpClient) {}

  create(identifier: IIdentifier): Observable<EntityResponseType> {
    return this.http.post<IIdentifier>(this.resourceUrl, identifier, { observe: 'response' });
  }

  update(identifier: IIdentifier): Observable<EntityResponseType> {
    return this.http.put<IIdentifier>(this.resourceUrl, identifier, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IIdentifier>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IIdentifier[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
}
