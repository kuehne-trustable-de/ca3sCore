/* tslint:disable max-line-length */
import axios from 'axios';

import * as config from '@/shared/config/config';
import {} from '@/shared/date/filters';
import AcmeAccountService from '@/entities/acme-account/acme-account.service';
import { AcmeAccount, AccountStatus } from '@/shared/model/acme-account.model';

const mockedAxios: any = axios;
jest.mock('axios', () => ({
  get: jest.fn(),
  post: jest.fn(),
  put: jest.fn(),
  delete: jest.fn()
}));

describe('Service Tests', () => {
  describe('AcmeAccount Service', () => {
    let service: AcmeAccountService;
    let elemDefault;
    beforeEach(() => {
      service = new AcmeAccountService();

      elemDefault = new AcmeAccount(0, 0, 'AAAAAAA', AccountStatus.VALID, false, 'AAAAAAA', 'AAAAAAA');
    });

    describe('Service methods', () => {
      it('should find an element', async () => {
        const returnedFromService = Object.assign({}, elemDefault);
        mockedAxios.get.mockReturnValue(Promise.resolve({ data: returnedFromService }));

        return service.find(123).then(res => {
          expect(res).toMatchObject(elemDefault);
        });
      });
      it('should create a AcmeAccount', async () => {
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

      it('should update a AcmeAccount', async () => {
        const returnedFromService = Object.assign(
          {
            accountId: 1,
            realm: 'BBBBBB',
            status: 'BBBBBB',
            termsOfServiceAgreed: true,
            publicKeyHash: 'BBBBBB',
            publicKey: 'BBBBBB'
          },
          elemDefault
        );

        const expected = Object.assign({}, returnedFromService);
        mockedAxios.put.mockReturnValue(Promise.resolve({ data: returnedFromService }));

        return service.update(expected).then(res => {
          expect(res).toMatchObject(expected);
        });
      });
      it('should return a list of AcmeAccount', async () => {
        const returnedFromService = Object.assign(
          {
            accountId: 1,
            realm: 'BBBBBB',
            status: 'BBBBBB',
            termsOfServiceAgreed: true,
            publicKeyHash: 'BBBBBB',
            publicKey: 'BBBBBB'
          },
          elemDefault
        );
        const expected = Object.assign({}, returnedFromService);
        mockedAxios.get.mockReturnValue(Promise.resolve([returnedFromService]));
        return service.retrieve().then(res => {
          expect(res).toContainEqual(expected);
        });
      });
      it('should delete a AcmeAccount', async () => {
        mockedAxios.delete.mockReturnValue(Promise.resolve({ ok: true }));
        return service.delete(123).then(res => {
          expect(res.ok).toBeTruthy();
        });
      });
    });
  });
});
