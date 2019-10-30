import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import * as moment from 'moment';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { DATE_FORMAT } from 'app/shared/constants/input.constants';
import { map } from 'rxjs/operators';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { IAcmeChallenge } from 'app/shared/model/acme-challenge.model';

type EntityResponseType = HttpResponse<IAcmeChallenge>;
type EntityArrayResponseType = HttpResponse<IAcmeChallenge[]>;

@Injectable({ providedIn: 'root' })
export class AcmeChallengeService {
  public resourceUrl = SERVER_API_URL + 'api/acme-challenges';

  constructor(protected http: HttpClient) {}

  create(acmeChallenge: IAcmeChallenge): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(acmeChallenge);
    return this.http
      .post<IAcmeChallenge>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(acmeChallenge: IAcmeChallenge): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(acmeChallenge);
    return this.http
      .put<IAcmeChallenge>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<IAcmeChallenge>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<IAcmeChallenge[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  protected convertDateFromClient(acmeChallenge: IAcmeChallenge): IAcmeChallenge {
    const copy: IAcmeChallenge = Object.assign({}, acmeChallenge, {
      validated: acmeChallenge.validated != null && acmeChallenge.validated.isValid() ? acmeChallenge.validated.format(DATE_FORMAT) : null
    });
    return copy;
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.validated = res.body.validated != null ? moment(res.body.validated) : null;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((acmeChallenge: IAcmeChallenge) => {
        acmeChallenge.validated = acmeChallenge.validated != null ? moment(acmeChallenge.validated) : null;
      });
    }
    return res;
  }
}
