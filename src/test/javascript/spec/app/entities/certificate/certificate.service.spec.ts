/* tslint:disable max-line-length */
import axios from 'axios';
import { format } from 'date-fns';

import * as config from '@/shared/config/config';
import { DATE_TIME_FORMAT } from '@/shared/date/filters';
import CertificateService from '@/entities/certificate/certificate.service';
import { Certificate } from '@/shared/model/certificate.model';

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
  describe('Certificate Service', () => {
    let service: CertificateService;
    let elemDefault;
    let currentDate: Date;
    beforeEach(() => {
      service = new CertificateService();
      currentDate = new Date();

      elemDefault = new Certificate(
        0,
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        currentDate,
        currentDate,
        'AAAAAAA',
        0,
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        'AAAAAAA',
        currentDate,
        currentDate,
        'AAAAAAA',
        false,
        'AAAAAAA',
        'AAAAAAA',
        false,
        false,
        false,
        false,
        'AAAAAAA'
      );
    });

    describe('Service methods', () => {
      it('should find an element', async () => {
        const returnedFromService = Object.assign(
          {
            validFrom: format(currentDate, DATE_TIME_FORMAT),
            validTo: format(currentDate, DATE_TIME_FORMAT),
            contentAddedAt: format(currentDate, DATE_TIME_FORMAT),
            revokedSince: format(currentDate, DATE_TIME_FORMAT),
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

      it('should create a Certificate', async () => {
        const returnedFromService = Object.assign(
          {
            id: 0,
            validFrom: format(currentDate, DATE_TIME_FORMAT),
            validTo: format(currentDate, DATE_TIME_FORMAT),
            contentAddedAt: format(currentDate, DATE_TIME_FORMAT),
            revokedSince: format(currentDate, DATE_TIME_FORMAT),
          },
          elemDefault
        );
        const expected = Object.assign(
          {
            validFrom: currentDate,
            validTo: currentDate,
            contentAddedAt: currentDate,
            revokedSince: currentDate,
          },
          returnedFromService
        );

        mockedAxios.post.mockReturnValue(Promise.resolve({ data: returnedFromService }));
        return service.create({}).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not create a Certificate', async () => {
        mockedAxios.post.mockReturnValue(Promise.reject(error));

        return service
          .create({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should update a Certificate', async () => {
        const returnedFromService = Object.assign(
          {
            tbsDigest: 'BBBBBB',
            subject: 'BBBBBB',
            sans: 'BBBBBB',
            issuer: 'BBBBBB',
            root: 'BBBBBB',
            type: 'BBBBBB',
            description: 'BBBBBB',
            fingerprint: 'BBBBBB',
            serial: 'BBBBBB',
            validFrom: format(currentDate, DATE_TIME_FORMAT),
            validTo: format(currentDate, DATE_TIME_FORMAT),
            keyAlgorithm: 'BBBBBB',
            keyLength: 1,
            curveName: 'BBBBBB',
            hashingAlgorithm: 'BBBBBB',
            paddingAlgorithm: 'BBBBBB',
            signingAlgorithm: 'BBBBBB',
            creationExecutionId: 'BBBBBB',
            contentAddedAt: format(currentDate, DATE_TIME_FORMAT),
            revokedSince: format(currentDate, DATE_TIME_FORMAT),
            revocationReason: 'BBBBBB',
            revoked: true,
            revocationExecutionId: 'BBBBBB',
            administrationComment: 'BBBBBB',
            endEntity: true,
            selfsigned: true,
            trusted: true,
            active: true,
            content: 'BBBBBB',
          },
          elemDefault
        );

        const expected = Object.assign(
          {
            validFrom: currentDate,
            validTo: currentDate,
            contentAddedAt: currentDate,
            revokedSince: currentDate,
          },
          returnedFromService
        );
        mockedAxios.put.mockReturnValue(Promise.resolve({ data: returnedFromService }));

        return service.update(expected).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not update a Certificate', async () => {
        mockedAxios.put.mockReturnValue(Promise.reject(error));

        return service
          .update({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should return a list of Certificate', async () => {
        const returnedFromService = Object.assign(
          {
            tbsDigest: 'BBBBBB',
            subject: 'BBBBBB',
            sans: 'BBBBBB',
            issuer: 'BBBBBB',
            root: 'BBBBBB',
            type: 'BBBBBB',
            description: 'BBBBBB',
            fingerprint: 'BBBBBB',
            serial: 'BBBBBB',
            validFrom: format(currentDate, DATE_TIME_FORMAT),
            validTo: format(currentDate, DATE_TIME_FORMAT),
            keyAlgorithm: 'BBBBBB',
            keyLength: 1,
            curveName: 'BBBBBB',
            hashingAlgorithm: 'BBBBBB',
            paddingAlgorithm: 'BBBBBB',
            signingAlgorithm: 'BBBBBB',
            creationExecutionId: 'BBBBBB',
            contentAddedAt: format(currentDate, DATE_TIME_FORMAT),
            revokedSince: format(currentDate, DATE_TIME_FORMAT),
            revocationReason: 'BBBBBB',
            revoked: true,
            revocationExecutionId: 'BBBBBB',
            administrationComment: 'BBBBBB',
            endEntity: true,
            selfsigned: true,
            trusted: true,
            active: true,
            content: 'BBBBBB',
          },
          elemDefault
        );
        const expected = Object.assign(
          {
            validFrom: currentDate,
            validTo: currentDate,
            contentAddedAt: currentDate,
            revokedSince: currentDate,
          },
          returnedFromService
        );
        mockedAxios.get.mockReturnValue(Promise.resolve([returnedFromService]));
        return service.retrieve({ sort: {}, page: 0, size: 10 }).then(res => {
          expect(res).toContainEqual(expected);
        });
      });

      it('should not return a list of Certificate', async () => {
        mockedAxios.get.mockReturnValue(Promise.reject(error));

        return service
          .retrieve()
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should delete a Certificate', async () => {
        mockedAxios.delete.mockReturnValue(Promise.resolve({ ok: true }));
        return service.delete(123).then(res => {
          expect(res.ok).toBeTruthy();
        });
      });

      it('should not delete a Certificate', async () => {
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
