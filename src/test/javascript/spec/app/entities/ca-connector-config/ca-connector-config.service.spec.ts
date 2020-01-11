/* tslint:disable max-line-length */
import axios from 'axios';

import * as config from '@/shared/config/config';
import {} from '@/shared/date/filters';
import CAConnectorConfigService from '@/entities/ca-connector-config/ca-connector-config.service';
import { CAConnectorConfig, CAConnectorType } from '@/shared/model/ca-connector-config.model';

const mockedAxios: any = axios;
jest.mock('axios', () => ({
  get: jest.fn(),
  post: jest.fn(),
  put: jest.fn(),
  delete: jest.fn()
}));

describe('Service Tests', () => {
  describe('CAConnectorConfig Service', () => {
    let service: CAConnectorConfigService;
    let elemDefault;
    beforeEach(() => {
      service = new CAConnectorConfigService();

      elemDefault = new CAConnectorConfig(0, 'AAAAAAA', CAConnectorType.INTERNAL, 'AAAAAAA', 'AAAAAAA', 0, false, false);
    });

    describe('Service methods', () => {
      it('should find an element', async () => {
        const returnedFromService = Object.assign({}, elemDefault);
        mockedAxios.get.mockReturnValue(Promise.resolve({ data: returnedFromService }));

        return service.find(123).then(res => {
          expect(res).toMatchObject(elemDefault);
        });
      });
      it('should create a CAConnectorConfig', async () => {
        const returnedFromService = Object.assign(
          {
            id: 0
          },
          elemDefault
        );
        const expected = Object.assign({}, returnedFromService);

        mockedAxios.post.mockReturnValue(Promise.resolve({ data: returnedFromService }));
        return service.create({}).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should update a CAConnectorConfig', async () => {
        const returnedFromService = Object.assign(
          {
            name: 'BBBBBB',
            caConnectorType: 'BBBBBB',
            caUrl: 'BBBBBB',
            secret: 'BBBBBB',
            pollingOffset: 1,
            defaultCA: true,
            active: true
          },
          elemDefault
        );

        const expected = Object.assign({}, returnedFromService);
        mockedAxios.put.mockReturnValue(Promise.resolve({ data: returnedFromService }));

        return service.update(expected).then(res => {
          expect(res).toMatchObject(expected);
        });
      });
      it('should return a list of CAConnectorConfig', async () => {
        const returnedFromService = Object.assign(
          {
            name: 'BBBBBB',
            caConnectorType: 'BBBBBB',
            caUrl: 'BBBBBB',
            secret: 'BBBBBB',
            pollingOffset: 1,
            defaultCA: true,
            active: true
          },
          elemDefault
        );
        const expected = Object.assign({}, returnedFromService);
        mockedAxios.get.mockReturnValue(Promise.resolve([returnedFromService]));
        return service.retrieve().then(res => {
          expect(res).toContainEqual(expected);
        });
      });
      it('should delete a CAConnectorConfig', async () => {
        mockedAxios.delete.mockReturnValue(Promise.resolve({ ok: true }));
        return service.delete(123).then(res => {
          expect(res.ok).toBeTruthy();
        });
      });
    });
  });
});
