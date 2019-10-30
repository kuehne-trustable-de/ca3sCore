import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { ICAConnectorConfig } from 'app/shared/model/ca-connector-config.model';

type EntityResponseType = HttpResponse<ICAConnectorConfig>;
type EntityArrayResponseType = HttpResponse<ICAConnectorConfig[]>;

@Injectable({ providedIn: 'root' })
export class CAConnectorConfigService {
  public resourceUrl = SERVER_API_URL + 'api/ca-connector-configs';

  constructor(protected http: HttpClient) {}

  create(cAConnectorConfig: ICAConnectorConfig): Observable<EntityResponseType> {
    return this.http.post<ICAConnectorConfig>(this.resourceUrl, cAConnectorConfig, { observe: 'response' });
  }

  update(cAConnectorConfig: ICAConnectorConfig): Observable<EntityResponseType> {
    return this.http.put<ICAConnectorConfig>(this.resourceUrl, cAConnectorConfig, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<ICAConnectorConfig>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<ICAConnectorConfig[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<any>> {
    return this.http.delete<any>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
}
