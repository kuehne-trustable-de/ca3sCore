import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IRequestAttributeValue } from 'app/shared/model/request-attribute-value.model';

type EntityResponseType = HttpResponse<IRequestAttributeValue>;
type EntityArrayResponseType = HttpResponse<IRequestAttributeValue[]>;

@Injectable({ providedIn: 'root' })
export class RequestAttributeValueService {
  public resourceUrl = SERVER_API_URL + 'api/request-attribute-values';

  constructor(protected http: HttpClient) {}

  create(requestAttributeValue: IRequestAttributeValue): Observable<EntityResponseType> {
    return this.http.post<IRequestAttributeValue>(this.resourceUrl, requestAttributeValue, { observe: 'response' });
  }

  update(requestAttributeValue: IRequestAttributeValue): Observable<EntityResponseType> {
    return this.http.put<IRequestAttributeValue>(this.resourceUrl, requestAttributeValue, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IRequestAttributeValue>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IRequestAttributeValue[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
}
