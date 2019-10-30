import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { ISelectorToTemplate } from 'app/shared/model/selector-to-template.model';

type EntityResponseType = HttpResponse<ISelectorToTemplate>;
type EntityArrayResponseType = HttpResponse<ISelectorToTemplate[]>;

@Injectable({ providedIn: 'root' })
export class SelectorToTemplateService {
  public resourceUrl = SERVER_API_URL + 'api/selector-to-templates';

  constructor(protected http: HttpClient) {}

  create(selectorToTemplate: ISelectorToTemplate): Observable<EntityResponseType> {
    return this.http.post<ISelectorToTemplate>(this.resourceUrl, selectorToTemplate, { observe: 'response' });
  }

  update(selectorToTemplate: ISelectorToTemplate): Observable<EntityResponseType> {
    return this.http.put<ISelectorToTemplate>(this.resourceUrl, selectorToTemplate, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<ISelectorToTemplate>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<ISelectorToTemplate[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
}
