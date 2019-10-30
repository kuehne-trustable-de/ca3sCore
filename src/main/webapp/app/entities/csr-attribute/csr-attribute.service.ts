import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { ICsrAttribute } from 'app/shared/model/csr-attribute.model';

type EntityResponseType = HttpResponse<ICsrAttribute>;
type EntityArrayResponseType = HttpResponse<ICsrAttribute[]>;

@Injectable({ providedIn: 'root' })
export class CsrAttributeService {
  public resourceUrl = SERVER_API_URL + 'api/csr-attributes';

  constructor(protected http: HttpClient) {}

  create(csrAttribute: ICsrAttribute): Observable<EntityResponseType> {
    return this.http.post<ICsrAttribute>(this.resourceUrl, csrAttribute, { observe: 'response' });
  }

  update(csrAttribute: ICsrAttribute): Observable<EntityResponseType> {
    return this.http.put<ICsrAttribute>(this.resourceUrl, csrAttribute, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<ICsrAttribute>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<ICsrAttribute[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
}
