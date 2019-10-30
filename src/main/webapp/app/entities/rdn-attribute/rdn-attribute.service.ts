import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IRDNAttribute } from 'app/shared/model/rdn-attribute.model';

type EntityResponseType = HttpResponse<IRDNAttribute>;
type EntityArrayResponseType = HttpResponse<IRDNAttribute[]>;

@Injectable({ providedIn: 'root' })
export class RDNAttributeService {
  public resourceUrl = SERVER_API_URL + 'api/rdn-attributes';

  constructor(protected http: HttpClient) {}

  create(rDNAttribute: IRDNAttribute): Observable<EntityResponseType> {
    return this.http.post<IRDNAttribute>(this.resourceUrl, rDNAttribute, { observe: 'response' });
  }

  update(rDNAttribute: IRDNAttribute): Observable<EntityResponseType> {
    return this.http.put<IRDNAttribute>(this.resourceUrl, rDNAttribute, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IRDNAttribute>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IRDNAttribute[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
}
