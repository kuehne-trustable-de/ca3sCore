import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IAcmeContact } from 'app/shared/model/acme-contact.model';

type EntityResponseType = HttpResponse<IAcmeContact>;
type EntityArrayResponseType = HttpResponse<IAcmeContact[]>;

@Injectable({ providedIn: 'root' })
export class AcmeContactService {
  public resourceUrl = SERVER_API_URL + 'api/acme-contacts';

  constructor(protected http: HttpClient) {}

  create(acmeContact: IAcmeContact): Observable<EntityResponseType> {
    return this.http.post<IAcmeContact>(this.resourceUrl, acmeContact, { observe: 'response' });
  }

  update(acmeContact: IAcmeContact): Observable<EntityResponseType> {
    return this.http.put<IAcmeContact>(this.resourceUrl, acmeContact, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IAcmeContact>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IAcmeContact[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
}
