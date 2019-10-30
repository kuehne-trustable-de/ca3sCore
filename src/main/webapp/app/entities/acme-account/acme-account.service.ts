import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IACMEAccount } from 'app/shared/model/acme-account.model';

type EntityResponseType = HttpResponse<IACMEAccount>;
type EntityArrayResponseType = HttpResponse<IACMEAccount[]>;

@Injectable({ providedIn: 'root' })
export class ACMEAccountService {
  public resourceUrl = SERVER_API_URL + 'api/acme-accounts';

  constructor(protected http: HttpClient) {}

  create(aCMEAccount: IACMEAccount): Observable<EntityResponseType> {
    return this.http.post<IACMEAccount>(this.resourceUrl, aCMEAccount, { observe: 'response' });
  }

  update(aCMEAccount: IACMEAccount): Observable<EntityResponseType> {
    return this.http.put<IACMEAccount>(this.resourceUrl, aCMEAccount, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IACMEAccount>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IACMEAccount[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
}
