import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IRDN } from 'app/shared/model/rdn.model';

type EntityResponseType = HttpResponse<IRDN>;
type EntityArrayResponseType = HttpResponse<IRDN[]>;

@Injectable({ providedIn: 'root' })
export class RDNService {
  public resourceUrl = SERVER_API_URL + 'api/rdns';

  constructor(protected http: HttpClient) {}

  create(rDN: IRDN): Observable<EntityResponseType> {
    return this.http.post<IRDN>(this.resourceUrl, rDN, { observe: 'response' });
  }

  update(rDN: IRDN): Observable<EntityResponseType> {
    return this.http.put<IRDN>(this.resourceUrl, rDN, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IRDN>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IRDN[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
}
