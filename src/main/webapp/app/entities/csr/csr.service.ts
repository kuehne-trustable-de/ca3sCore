import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import * as moment from 'moment';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { DATE_FORMAT } from 'app/shared/constants/input.constants';
import { map } from 'rxjs/operators';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { ICSR } from 'app/shared/model/csr.model';

type EntityResponseType = HttpResponse<ICSR>;
type EntityArrayResponseType = HttpResponse<ICSR[]>;

@Injectable({ providedIn: 'root' })
export class CSRService {
  public resourceUrl = SERVER_API_URL + 'api/csrs';

  constructor(protected http: HttpClient) {}

  create(cSR: ICSR): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(cSR);
    return this.http
      .post<ICSR>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(cSR: ICSR): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(cSR);
    return this.http
      .put<ICSR>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<ICSR>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<ICSR[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  protected convertDateFromClient(cSR: ICSR): ICSR {
    const copy: ICSR = Object.assign({}, cSR, {
      requestedOn: cSR.requestedOn != null && cSR.requestedOn.isValid() ? cSR.requestedOn.format(DATE_FORMAT) : null
    });
    return copy;
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.requestedOn = res.body.requestedOn != null ? moment(res.body.requestedOn) : null;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((cSR: ICSR) => {
        cSR.requestedOn = cSR.requestedOn != null ? moment(cSR.requestedOn) : null;
      });
    }
    return res;
  }
}
