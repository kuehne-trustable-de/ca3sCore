import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { ICertificateAttribute } from 'app/shared/model/certificate-attribute.model';

type EntityResponseType = HttpResponse<ICertificateAttribute>;
type EntityArrayResponseType = HttpResponse<ICertificateAttribute[]>;

@Injectable({ providedIn: 'root' })
export class CertificateAttributeService {
  public resourceUrl = SERVER_API_URL + 'api/certificate-attributes';

  constructor(protected http: HttpClient) {}

  create(certificateAttribute: ICertificateAttribute): Observable<EntityResponseType> {
    return this.http.post<ICertificateAttribute>(this.resourceUrl, certificateAttribute, { observe: 'response' });
  }

  update(certificateAttribute: ICertificateAttribute): Observable<EntityResponseType> {
    return this.http.put<ICertificateAttribute>(this.resourceUrl, certificateAttribute, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<ICertificateAttribute>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<ICertificateAttribute[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
}
