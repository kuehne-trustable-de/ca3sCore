import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IAuthorization } from 'app/shared/model/authorization.model';

type EntityResponseType = HttpResponse<IAuthorization>;
type EntityArrayResponseType = HttpResponse<IAuthorization[]>;

@Injectable({ providedIn: 'root' })
export class AuthorizationService {
  public resourceUrl = SERVER_API_URL + 'api/authorizations';

  constructor(protected http: HttpClient) {}

  create(authorization: IAuthorization): Observable<EntityResponseType> {
    return this.http.post<IAuthorization>(this.resourceUrl, authorization, { observe: 'response' });
  }

  update(authorization: IAuthorization): Observable<EntityResponseType> {
    return this.http.put<IAuthorization>(this.resourceUrl, authorization, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IAuthorization>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IAuthorization[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
}
