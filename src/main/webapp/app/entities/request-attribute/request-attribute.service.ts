import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IRequestAttribute } from 'app/shared/model/request-attribute.model';

type EntityResponseType = HttpResponse<IRequestAttribute>;
type EntityArrayResponseType = HttpResponse<IRequestAttribute[]>;

@Injectable({ providedIn: 'root' })
export class RequestAttributeService {
  public resourceUrl = SERVER_API_URL + 'api/request-attributes';

  constructor(protected http: HttpClient) {}

  create(requestAttribute: IRequestAttribute): Observable<EntityResponseType> {
    return this.http.post<IRequestAttribute>(this.resourceUrl, requestAttribute, { observe: 'response' });
  }

  update(requestAttribute: IRequestAttribute): Observable<EntityResponseType> {
    return this.http.put<IRequestAttribute>(this.resourceUrl, requestAttribute, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IRequestAttribute>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IRequestAttribute[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
}
