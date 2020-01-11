/* tslint:disable max-line-length */
import axios from 'axios';
import { format } from 'date-fns';

import * as config from '@/shared/config/config';
import { DATE_FORMAT } from '@/shared/date/filters';
import AcmeChallengeService from '@/entities/acme-challenge/acme-challenge.service';
import { AcmeChallenge, ChallengeStatus } from '@/shared/model/acme-challenge.model';

const mockedAxios: any = axios;
jest.mock('axios', () => ({
  get: jest.fn(),
  post: jest.fn(),
  put: jest.fn(),
  delete: jest.fn()
}));

describe('Service Tests', () => {
  describe('AcmeChallenge Service', () => {
    let service: AcmeChallengeService;
    let elemDefault;
    let currentDate: Date;
    beforeEach(() => {
      service = new AcmeChallengeService();
      currentDate = new Date();

      elemDefault = new AcmeChallenge(0, 0, 'AAAAAAA', 'AAAAAAA', 'AAAAAAA', currentDate, ChallengeStatus.PENDING);
    });

    describe('Service methods', () => {
      it('should find an element', async () => {
        const returnedFromService = Object.assign(
          {
            validated: format(currentDate, DATE_FORMAT)
          },
          elemDefault
        );
        mockedAxios.get.mockReturnValue(Promise.resolve({ data: returnedFromService }));

        return service.find(123).then(res => {
          expect(res).toMatchObject(elemDefault);
        });
      });
      it('should create a AcmeChallenge', async () => {
        const returnedFromService = Object.assign(
          {
            id: 0,
            validated: format(currentDate, DATE_FORMAT)
          },
          elemDefault
        );
        const expected = Object.assign(
          {
            validated: currentDate
          },
          returnedFromService
        );

        mockedAxios.post.mockReturnValue(Promise.resolve({ data: returnedFromService }));
        return service.create({}).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should update a AcmeChallenge', async () => {
        const returnedFromService = Object.assign(
          {
            challengeId: 1,
            type: 'BBBBBB',
            value: 'BBBBBB',
            token: 'BBBBBB',
            validated: format(currentDate, DATE_FORMAT),
            status: 'BBBBBB'
          },
          elemDefault
        );

        const expected = Object.assign(
          {
            validated: currentDate
          },
          returnedFromService
        );
        mockedAxios.put.mockReturnValue(Promise.resolve({ data: returnedFromService }));

        return service.update(expected).then(res => {
          expect(res).toMatchObject(expected);
        });
      });
      it('should return a list of AcmeChallenge', async () => {
        const returnedFromService = Object.assign(
          {
            challengeId: 1,
            type: 'BBBBBB',
            value: 'BBBBBB',
            token: 'BBBBBB',
            validated: format(currentDate, DATE_FORMAT),
            status: 'BBBBBB'
          },
          elemDefault
        );
        const expected = Object.assign(
          {
            validated: currentDate
          },
          returnedFromService
        );
        mockedAxios.get.mockReturnValue(Promise.resolve([returnedFromService]));
        return service.retrieve().then(res => {
          expect(res).toContainEqual(expected);
        });
      });
      it('should delete a AcmeChallenge', async () => {
        mockedAxios.delete.mockReturnValue(Promise.resolve({ ok: true }));
        return service.delete(123).then(res => {
          expect(res.ok).toBeTruthy();
        });
      });
    });
  });
});
