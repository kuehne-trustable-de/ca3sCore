/* tslint:disable max-line-length */
import axios from 'axios';
import { format } from 'date-fns';

import * as config from '@/shared/config/config';
import { DATE_TIME_FORMAT } from '@/shared/date/filters';
import CSRService from '@/entities/csr/csr.service';
import { CSR, PipelineType, CsrStatus } from '@/shared/model/csr.model';

const mockedAxios: any = axios;
const error = {
  response: {
    status: null,
    data: {
      type: null
    }
  }
};

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
        'AAAAAAA',
        currentDate,
        'AAAAAAA',
        PipelineType.ACME,
        CsrStatus.PROCESSING,
        'AAAAAAA',
        currentDate,
        currentDate,
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        false,
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        0,
        'AAAAAAA',
        false,
        'AAAAAAA'
      );
    });

    describe('Service methods', () => {
      it('should find an element', async () => {
        const returnedFromService = Object.assign(
          {
            requestedOn: format(currentDate, DATE_TIME_FORMAT),
            approvedOn: format(currentDate, DATE_TIME_FORMAT),
            rejectedOn: format(currentDate, DATE_TIME_FORMAT)
          },
          elemDefault
        );
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

      it('should create a CSR', async () => {
        const returnedFromService = Object.assign(
          {
            id: 0,
            requestedOn: format(currentDate, DATE_TIME_FORMAT),
            approvedOn: format(currentDate, DATE_TIME_FORMAT),
            rejectedOn: format(currentDate, DATE_TIME_FORMAT)
          },
          elemDefault
        );
        const expected = Object.assign(
          {
            requestedOn: currentDate,
            approvedOn: currentDate,
            rejectedOn: currentDate
          },
          returnedFromService
        );

        mockedAxios.post.mockReturnValue(Promise.resolve({ data: returnedFromService }));
        return service.create({}).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not create a CSR', async () => {
        mockedAxios.post.mockReturnValue(Promise.reject(error));

        return service
          .create({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should update a CSR', async () => {
        const returnedFromService = Object.assign(
          {
            csrBase64: 'BBBBBB',
            subject: 'BBBBBB',
            requestedOn: format(currentDate, DATE_TIME_FORMAT),
            requestedBy: 'BBBBBB',
            pipelineType: 'BBBBBB',
            status: 'BBBBBB',
            administeredBy: 'BBBBBB',
            approvedOn: format(currentDate, DATE_TIME_FORMAT),
            rejectedOn: format(currentDate, DATE_TIME_FORMAT),
            rejectionReason: 'BBBBBB',
            processInstanceId: 'BBBBBB',
            signingAlgorithm: 'BBBBBB',
            isCSRValid: true,
            x509KeySpec: 'BBBBBB',
            publicKeyAlgorithm: 'BBBBBB',
            keyAlgorithm: 'BBBBBB',
            keyLength: 1,
            publicKeyHash: 'BBBBBB',
            serversideKeyGeneration: true,
            subjectPublicKeyInfoBase64: 'BBBBBB'
          },
          elemDefault
        );

        const expected = Object.assign(
          {
            requestedOn: currentDate,
            approvedOn: currentDate,
            rejectedOn: currentDate
          },
          returnedFromService
        );
        mockedAxios.put.mockReturnValue(Promise.resolve({ data: returnedFromService }));

        return service.update(expected).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not update a CSR', async () => {
        mockedAxios.put.mockReturnValue(Promise.reject(error));

        return service
          .update({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should return a list of CSR', async () => {
        const returnedFromService = Object.assign(
          {
            csrBase64: 'BBBBBB',
            subject: 'BBBBBB',
            requestedOn: format(currentDate, DATE_TIME_FORMAT),
            requestedBy: 'BBBBBB',
            pipelineType: 'BBBBBB',
            status: 'BBBBBB',
            administeredBy: 'BBBBBB',
            approvedOn: format(currentDate, DATE_TIME_FORMAT),
            rejectedOn: format(currentDate, DATE_TIME_FORMAT),
            rejectionReason: 'BBBBBB',
            processInstanceId: 'BBBBBB',
            signingAlgorithm: 'BBBBBB',
            isCSRValid: true,
            x509KeySpec: 'BBBBBB',
            publicKeyAlgorithm: 'BBBBBB',
            keyAlgorithm: 'BBBBBB',
            keyLength: 1,
            publicKeyHash: 'BBBBBB',
            serversideKeyGeneration: true,
            subjectPublicKeyInfoBase64: 'BBBBBB'
          },
          elemDefault
        );
        const expected = Object.assign(
          {
            requestedOn: currentDate,
            approvedOn: currentDate,
            rejectedOn: currentDate
          },
          returnedFromService
        );
        mockedAxios.get.mockReturnValue(Promise.resolve([returnedFromService]));
        return service.retrieve().then(res => {
          expect(res).toContainEqual(expected);
        });
      });

      it('should not return a list of CSR', async () => {
        mockedAxios.get.mockReturnValue(Promise.reject(error));

        return service
          .retrieve()
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should delete a CSR', async () => {
        mockedAxios.delete.mockReturnValue(Promise.resolve({ ok: true }));
        return service.delete(123).then(res => {
          expect(res.ok).toBeTruthy();
        });
      });

      it('should not delete a CSR', async () => {
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
