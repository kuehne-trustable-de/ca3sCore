/* tslint:disable max-line-length */
import axios from 'axios';
import { format } from 'date-fns';

import * as config from '@/shared/config/config';
import { DATE_TIME_FORMAT } from '@/shared/date/filters';
import CSRService from '@/entities/csr/csr.service';
import { CSR, CsrStatus } from '@/shared/model/csr.model';

const mockedAxios: any = axios;
jest.mock('axios', () => ({
  get: jest.fn(),
  post: jest.fn(),
  put: jest.fn(),
  delete: jest.fn()
}));

describe('Service Tests', () => {
  describe('CSR Service', () => {
    let service: CSRService;
    let elemDefault;
    let currentDate: Date;
    beforeEach(() => {
      service = new CSRService();
      currentDate = new Date();

      elemDefault = new CSR(
        0,
        'AAAAAAA',
        currentDate,
        CsrStatus.PROCESSING,
        'AAAAAAA',
        'AAAAAAA',
        false,
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA'
      );
    });

    describe('Service methods', () => {
      it('should find an element', async () => {
        const returnedFromService = Object.assign(
          {
            requestedOn: format(currentDate, DATE_TIME_FORMAT)
          },
          elemDefault
        );
        mockedAxios.get.mockReturnValue(Promise.resolve({ data: returnedFromService }));

        return service.find(123).then(res => {
          expect(res).toMatchObject(elemDefault);
        });
      });
      it('should create a CSR', async () => {
        const returnedFromService = Object.assign(
          {
            id: 0,
            requestedOn: format(currentDate, DATE_TIME_FORMAT)
          },
          elemDefault
        );
        const expected = Object.assign(
          {
            requestedOn: currentDate
          },
          returnedFromService
        );

        mockedAxios.post.mockReturnValue(Promise.resolve({ data: returnedFromService }));
        return service.create({}).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should update a CSR', async () => {
        const returnedFromService = Object.assign(
          {
            csrBase64: 'BBBBBB',
            requestedOn: format(currentDate, DATE_TIME_FORMAT),
            status: 'BBBBBB',
            processInstanceId: 'BBBBBB',
            signingAlgorithm: 'BBBBBB',
            isCSRValid: true,
            x509KeySpec: 'BBBBBB',
            publicKeyAlgorithm: 'BBBBBB',
            publicKeyHash: 'BBBBBB',
            subjectPublicKeyInfoBase64: 'BBBBBB'
          },
          elemDefault
        );

        const expected = Object.assign(
          {
            requestedOn: currentDate
          },
          returnedFromService
        );
        mockedAxios.put.mockReturnValue(Promise.resolve({ data: returnedFromService }));

        return service.update(expected).then(res => {
          expect(res).toMatchObject(expected);
        });
      });
      it('should return a list of CSR', async () => {
        const returnedFromService = Object.assign(
          {
            csrBase64: 'BBBBBB',
            requestedOn: format(currentDate, DATE_TIME_FORMAT),
            status: 'BBBBBB',
            processInstanceId: 'BBBBBB',
            signingAlgorithm: 'BBBBBB',
            isCSRValid: true,
            x509KeySpec: 'BBBBBB',
            publicKeyAlgorithm: 'BBBBBB',
            publicKeyHash: 'BBBBBB',
            subjectPublicKeyInfoBase64: 'BBBBBB'
          },
          elemDefault
        );
        const expected = Object.assign(
          {
            requestedOn: currentDate
          },
          returnedFromService
        );
        mockedAxios.get.mockReturnValue(Promise.resolve([returnedFromService]));
        return service.retrieve().then(res => {
          expect(res).toContainEqual(expected);
        });
      });
      it('should delete a CSR', async () => {
        mockedAxios.delete.mockReturnValue(Promise.resolve({ ok: true }));
        return service.delete(123).then(res => {
          expect(res.ok).toBeTruthy();
        });
      });
    });
  });
});
