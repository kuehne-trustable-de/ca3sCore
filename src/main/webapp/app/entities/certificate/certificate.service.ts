import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import * as moment from 'moment';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { DATE_FORMAT } from 'app/shared/constants/input.constants';
import { map } from 'rxjs/operators';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { ICertificate } from 'app/shared/model/certificate.model';

type EntityResponseType = HttpResponse<ICertificate>;
type EntityArrayResponseType = HttpResponse<ICertificate[]>;

@Injectable({ providedIn: 'root' })
export class CertificateService {
  public resourceUrl = SERVER_API_URL + 'api/certificates';

  constructor(protected http: HttpClient) {}

  create(certificate: ICertificate): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(certificate);
    return this.http
      .post<ICertificate>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(certificate: ICertificate): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(certificate);
    return this.http
      .put<ICertificate>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<ICertificate>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<ICertificate[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  protected convertDateFromClient(certificate: ICertificate): ICertificate {
    const copy: ICertificate = Object.assign({}, certificate, {
      validFrom: certificate.validFrom != null && certificate.validFrom.isValid() ? certificate.validFrom.format(DATE_FORMAT) : null,
      validTo: certificate.validTo != null && certificate.validTo.isValid() ? certificate.validTo.format(DATE_FORMAT) : null,
      contentAddedAt:
        certificate.contentAddedAt != null && certificate.contentAddedAt.isValid() ? certificate.contentAddedAt.format(DATE_FORMAT) : null,
      revokedSince:
        certificate.revokedSince != null && certificate.revokedSince.isValid() ? certificate.revokedSince.format(DATE_FORMAT) : null
    });
    return copy;
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.validFrom = res.body.validFrom != null ? moment(res.body.validFrom) : null;
      res.body.validTo = res.body.validTo != null ? moment(res.body.validTo) : null;
      res.body.contentAddedAt = res.body.contentAddedAt != null ? moment(res.body.contentAddedAt) : null;
      res.body.revokedSince = res.body.revokedSince != null ? moment(res.body.revokedSince) : null;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((certificate: ICertificate) => {
        certificate.validFrom = certificate.validFrom != null ? moment(certificate.validFrom) : null;
        certificate.validTo = certificate.validTo != null ? moment(certificate.validTo) : null;
        certificate.contentAddedAt = certificate.contentAddedAt != null ? moment(certificate.contentAddedAt) : null;
        certificate.revokedSince = certificate.revokedSince != null ? moment(certificate.revokedSince) : null;
      });
    }
    return res;
  }
}
