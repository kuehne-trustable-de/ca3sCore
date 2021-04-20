/* tslint:disable max-line-length */
import axios from 'axios';
import { format } from 'date-fns';

import * as config from '@/shared/config/config';
import { DATE_TIME_FORMAT } from '@/shared/date/filters';
import BPNMProcessInfoService from '@/entities/bpnm-process-info/bpnm-process-info.service';
import { BPNMProcessInfo, BPNMProcessType } from '@/shared/model/bpmn-process-info.model';

const mockedAxios: any = axios;
jest.mock('axios', () => ({
  get: jest.fn(),
  post: jest.fn(),
  put: jest.fn(),
  delete: jest.fn()
}));

describe('Service Tests', () => {
  describe('BPNMProcessInfo Service', () => {
    let service: BPNMProcessInfoService;
    let elemDefault;
    let currentDate: Date;
    beforeEach(() => {
      service = new BPNMProcessInfoService();
      currentDate = new Date();

      elemDefault = new BPNMProcessInfo(0, 'AAAAAAA', 'AAAAAAA', BPNMProcessType.CA_INVOCATION, 'AAAAAAA', currentDate, 'AAAAAAA');
    });

    describe('Service methods', () => {
      it('should find an element', async () => {
        const returnedFromService = Object.assign(
          {
            lastChange: format(currentDate, DATE_TIME_FORMAT)
          },
          elemDefault
        );
        mockedAxios.get.mockReturnValue(Promise.resolve({ data: returnedFromService }));

        return service.find(123).then(res => {
          expect(res).toMatchObject(elemDefault);
        });
      });
      it('should create a BPNMProcessInfo', async () => {
        const returnedFromService = Object.assign(
          {
            id: 0,
            lastChange: format(currentDate, DATE_TIME_FORMAT)
          },
          elemDefault
        );
        const expected = Object.assign(
          {
            lastChange: currentDate
          },
          returnedFromService
        );

        mockedAxios.post.mockReturnValue(Promise.resolve({ data: returnedFromService }));
        return service.create({}).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should update a BPNMProcessInfo', async () => {
        const returnedFromService = Object.assign(
          {
            name: 'BBBBBB',
            version: 'BBBBBB',
            type: 'BBBBBB',
            author: 'BBBBBB',
            lastChange: format(currentDate, DATE_TIME_FORMAT),
            signatureBase64: 'BBBBBB'
          },
          elemDefault
        );

        const expected = Object.assign(
          {
            lastChange: currentDate
          },
          returnedFromService
        );
        mockedAxios.put.mockReturnValue(Promise.resolve({ data: returnedFromService }));

        return service.update(expected).then(res => {
          expect(res).toMatchObject(expected);
        });
      });
      it('should return a list of BPNMProcessInfo', async () => {
        const returnedFromService = Object.assign(
          {
            name: 'BBBBBB',
            version: 'BBBBBB',
            type: 'BBBBBB',
            author: 'BBBBBB',
            lastChange: format(currentDate, DATE_TIME_FORMAT),
            signatureBase64: 'BBBBBB'
          },
          elemDefault
        );
        const expected = Object.assign(
          {
            lastChange: currentDate
          },
          returnedFromService
        );
        mockedAxios.get.mockReturnValue(Promise.resolve([returnedFromService]));
        return service.retrieve().then(res => {
          expect(res).toContainEqual(expected);
        });
      });
      it('should delete a BPNMProcessInfo', async () => {
        mockedAxios.delete.mockReturnValue(Promise.resolve({ ok: true }));
        return service.delete(123).then(res => {
          expect(res.ok).toBeTruthy();
        });
      });
    });
  });
});
