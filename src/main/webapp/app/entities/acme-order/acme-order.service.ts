import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import * as moment from 'moment';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { DATE_FORMAT } from 'app/shared/constants/input.constants';
import { map } from 'rxjs/operators';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IAcmeOrder } from 'app/shared/model/acme-order.model';

type EntityResponseType = HttpResponse<IAcmeOrder>;
type EntityArrayResponseType = HttpResponse<IAcmeOrder[]>;

@Injectable({ providedIn: 'root' })
export class AcmeOrderService {
  public resourceUrl = SERVER_API_URL + 'api/acme-orders';

  constructor(protected http: HttpClient) {}

  create(acmeOrder: IAcmeOrder): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(acmeOrder);
    return this.http
      .post<IAcmeOrder>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(acmeOrder: IAcmeOrder): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(acmeOrder);
    return this.http
      .put<IAcmeOrder>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<IAcmeOrder>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<IAcmeOrder[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  protected convertDateFromClient(acmeOrder: IAcmeOrder): IAcmeOrder {
    const copy: IAcmeOrder = Object.assign({}, acmeOrder, {
      expires: acmeOrder.expires != null && acmeOrder.expires.isValid() ? acmeOrder.expires.format(DATE_FORMAT) : null,
      notBefore: acmeOrder.notBefore != null && acmeOrder.notBefore.isValid() ? acmeOrder.notBefore.format(DATE_FORMAT) : null,
      notAfter: acmeOrder.notAfter != null && acmeOrder.notAfter.isValid() ? acmeOrder.notAfter.format(DATE_FORMAT) : null
    });
    return copy;
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.expires = res.body.expires != null ? moment(res.body.expires) : null;
      res.body.notBefore = res.body.notBefore != null ? moment(res.body.notBefore) : null;
      res.body.notAfter = res.body.notAfter != null ? moment(res.body.notAfter) : null;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((acmeOrder: IAcmeOrder) => {
        acmeOrder.expires = acmeOrder.expires != null ? moment(acmeOrder.expires) : null;
        acmeOrder.notBefore = acmeOrder.notBefore != null ? moment(acmeOrder.notBefore) : null;
        acmeOrder.notAfter = acmeOrder.notAfter != null ? moment(acmeOrder.notAfter) : null;
      });
    }
    return res;
  }
}
