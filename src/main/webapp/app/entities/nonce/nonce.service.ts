import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import * as moment from 'moment';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { DATE_FORMAT } from 'app/shared/constants/input.constants';
import { map } from 'rxjs/operators';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { INonce } from 'app/shared/model/nonce.model';

type EntityResponseType = HttpResponse<INonce>;
type EntityArrayResponseType = HttpResponse<INonce[]>;

@Injectable({ providedIn: 'root' })
export class NonceService {
  public resourceUrl = SERVER_API_URL + 'api/nonces';

  constructor(protected http: HttpClient) {}

  create(nonce: INonce): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(nonce);
    return this.http
      .post<INonce>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(nonce: INonce): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(nonce);
    return this.http
      .put<INonce>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<INonce>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<INonce[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  protected convertDateFromClient(nonce: INonce): INonce {
    const copy: INonce = Object.assign({}, nonce, {
      expiresAt: nonce.expiresAt != null && nonce.expiresAt.isValid() ? nonce.expiresAt.format(DATE_FORMAT) : null
    });
    return copy;
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.expiresAt = res.body.expiresAt != null ? moment(res.body.expiresAt) : null;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((nonce: INonce) => {
        nonce.expiresAt = nonce.expiresAt != null ? moment(nonce.expiresAt) : null;
      });
    }
    return res;
  }
}
