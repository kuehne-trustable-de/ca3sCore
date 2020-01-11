/* tslint:disable max-line-length */
import axios from 'axios';
import { format } from 'date-fns';

import * as config from '@/shared/config/config';
import { DATE_FORMAT } from '@/shared/date/filters';
import AcmeOrderService from '@/entities/acme-order/acme-order.service';
import { AcmeOrder, OrderStatus } from '@/shared/model/acme-order.model';

const mockedAxios: any = axios;
jest.mock('axios', () => ({
  get: jest.fn(),
  post: jest.fn(),
  put: jest.fn(),
  delete: jest.fn()
}));

describe('Service Tests', () => {
  describe('AcmeOrder Service', () => {
    let service: AcmeOrderService;
    let elemDefault;
    let currentDate: Date;
    beforeEach(() => {
      service = new AcmeOrderService();
      currentDate = new Date();

      elemDefault = new AcmeOrder(0, 0, OrderStatus.PENDING, currentDate, currentDate, currentDate, 'AAAAAAA', 'AAAAAAA', 'AAAAAAA');
    });

    describe('Service methods', () => {
      it('should find an element', async () => {
        const returnedFromService = Object.assign(
          {
            expires: format(currentDate, DATE_FORMAT),
            notBefore: format(currentDate, DATE_FORMAT),
            notAfter: format(currentDate, DATE_FORMAT)
          },
          elemDefault
        );
        mockedAxios.get.mockReturnValue(Promise.resolve({ data: returnedFromService }));

        return service.find(123).then(res => {
          expect(res).toMatchObject(elemDefault);
        });
      });
      it('should create a AcmeOrder', async () => {
        const returnedFromService = Object.assign(
          {
            id: 0,
            expires: format(currentDate, DATE_FORMAT),
            notBefore: format(currentDate, DATE_FORMAT),
            notAfter: format(currentDate, DATE_FORMAT)
          },
          elemDefault
        );
        const expected = Object.assign(
          {
            expires: currentDate,
            notBefore: currentDate,
            notAfter: currentDate
          },
          returnedFromService
        );

        mockedAxios.post.mockReturnValue(Promise.resolve({ data: returnedFromService }));
        return service.create({}).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should update a AcmeOrder', async () => {
        const returnedFromService = Object.assign(
          {
            orderId: 1,
            status: 'BBBBBB',
            expires: format(currentDate, DATE_FORMAT),
            notBefore: format(currentDate, DATE_FORMAT),
            notAfter: format(currentDate, DATE_FORMAT),
            error: 'BBBBBB',
            finalizeUrl: 'BBBBBB',
            certificateUrl: 'BBBBBB'
          },
          elemDefault
        );

        const expected = Object.assign(
          {
            expires: currentDate,
            notBefore: currentDate,
            notAfter: currentDate
          },
          returnedFromService
        );
        mockedAxios.put.mockReturnValue(Promise.resolve({ data: returnedFromService }));

        return service.update(expected).then(res => {
          expect(res).toMatchObject(expected);
        });
      });
      it('should return a list of AcmeOrder', async () => {
        const returnedFromService = Object.assign(
          {
            orderId: 1,
            status: 'BBBBBB',
            expires: format(currentDate, DATE_FORMAT),
            notBefore: format(currentDate, DATE_FORMAT),
            notAfter: format(currentDate, DATE_FORMAT),
            error: 'BBBBBB',
            finalizeUrl: 'BBBBBB',
            certificateUrl: 'BBBBBB'
          },
          elemDefault
        );
        const expected = Object.assign(
          {
            expires: currentDate,
            notBefore: currentDate,
            notAfter: currentDate
          },
          returnedFromService
        );
        mockedAxios.get.mockReturnValue(Promise.resolve([returnedFromService]));
        return service.retrieve().then(res => {
          expect(res).toContainEqual(expected);
        });
      });
      it('should delete a AcmeOrder', async () => {
        mockedAxios.delete.mockReturnValue(Promise.resolve({ ok: true }));
        return service.delete(123).then(res => {
          expect(res.ok).toBeTruthy();
        });
      });
    });
  });
});
