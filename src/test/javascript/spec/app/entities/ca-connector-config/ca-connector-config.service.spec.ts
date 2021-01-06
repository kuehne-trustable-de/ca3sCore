/* tslint:disable max-line-length */
import axios from 'axios';

import * as config from '@/shared/config/config';
import {} from '@/shared/date/filters';
import CAConnectorConfigService from '@/entities/ca-connector-config/ca-connector-config.service';
import { CAConnectorConfig, CAConnectorType, Interval } from '@/shared/model/ca-connector-config.model';

const mockedAxios: any = axios;
const error = {
  response: {
    status: null,
    data: {
      type: null,
    },
  },
};

jest.mock('axios', () => ({
  get: jest.fn(),
  post: jest.fn(),
  put: jest.fn(),
  delete: jest.fn(),
}));

describe('Service Tests', () => {
  describe('CAConnectorConfig Service', () => {
    let service: CAConnectorConfigService;
    let elemDefault;
    beforeEach(() => {
      service = new CAConnectorConfigService();

      elemDefault = new CAConnectorConfig(
        0,
        'AAAAAAA',
        CAConnectorType.INTERNAL,
        'AAAAAAA',
        0,
        false,
        false,
        'AAAAAAA',
        Interval.MINUTE,
        'AAAAAAA'
      );
    });

    describe('Service methods', () => {
      it('should find an element', async () => {
        const returnedFromService = Object.assign({}, elemDefault);
        mockedAxios.get.mockReturnValue(Promise.resolve({ data: returnedFromService }));

        return service.find(123).then(res => {
          expect(res).toMatchObject(elemDefault);
        });
      });

      it('should not find an element', async () => {
        mockedAxios.get.mockReturnValue(Promise.reject(error));
        return service
          .find(123)
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should create a CAConnectorConfig', async () => {
        const returnedFromService = Object.assign(
          {
            id: 0,
          },
          elemDefault
        );
        const expected = Object.assign({}, returnedFromService);

        mockedAxios.post.mockReturnValue(Promise.resolve({ data: returnedFromService }));
        return service.create({}).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not create a CAConnectorConfig', async () => {
        mockedAxios.post.mockReturnValue(Promise.reject(error));

        return service
          .create({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should update a CAConnectorConfig', async () => {
        const returnedFromService = Object.assign(
          {
            name: 'BBBBBB',
            caConnectorType: 'BBBBBB',
            caUrl: 'BBBBBB',
            pollingOffset: 1,
            defaultCA: true,
            active: true,
            selector: 'BBBBBB',
            interval: 'BBBBBB',
            plainSecret: 'BBBBBB',
          },
          elemDefault
        );

        const expected = Object.assign({}, returnedFromService);
        mockedAxios.put.mockReturnValue(Promise.resolve({ data: returnedFromService }));

        return service.update(expected).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not update a CAConnectorConfig', async () => {
        mockedAxios.put.mockReturnValue(Promise.reject(error));

        return service
          .update({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should return a list of CAConnectorConfig', async () => {
        const returnedFromService = Object.assign(
          {
            name: 'BBBBBB',
            caConnectorType: 'BBBBBB',
            caUrl: 'BBBBBB',
            pollingOffset: 1,
            defaultCA: true,
            active: true,
            selector: 'BBBBBB',
            interval: 'BBBBBB',
            plainSecret: 'BBBBBB',
          },
          elemDefault
        );
        const expected = Object.assign({}, returnedFromService);
        mockedAxios.get.mockReturnValue(Promise.resolve([returnedFromService]));
        return service.retrieve().then(res => {
          expect(res).toContainEqual(expected);
        });
      });

      it('should not return a list of CAConnectorConfig', async () => {
        mockedAxios.get.mockReturnValue(Promise.reject(error));

        return service
          .retrieve()
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should delete a CAConnectorConfig', async () => {
        mockedAxios.delete.mockReturnValue(Promise.resolve({ ok: true }));
        return service.delete(123).then(res => {
          expect(res.ok).toBeTruthy();
        });
      });

      it('should not delete a CAConnectorConfig', async () => {
        mockedAxios.delete.mockReturnValue(Promise.reject(error));

        return service
          .delete(123)
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });
    });
  });
});
