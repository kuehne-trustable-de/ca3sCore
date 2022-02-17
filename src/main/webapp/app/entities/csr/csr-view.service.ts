import axios from 'axios';

import buildPaginationQueryOpts from '@/shared/sort/sorts';

import { ICSRView } from '@/shared/model/transfer-object.model';

const baseApiUrl = 'api/csrView';

export default class CSRViewService {
  public find(id: number): Promise<ICSRView> {
    return new Promise<ICSRView>((resolve, reject) => {
      axios
        .get(`${baseApiUrl}/${id}`)
        .then(function(res) {
          resolve(res.data);
        })
        .catch(err => {
          reject(err);
        });
    });
  }

  public retrieve(paginationQuery?: any): Promise<any> {
    return new Promise<any>((resolve, reject) => {
      axios
        .get(baseApiUrl + `?${buildPaginationQueryOpts(paginationQuery)}`)
        .then(function(res) {
          resolve(res);
        })
        .catch(err => {
          reject(err);
        });
    });
  }
}
