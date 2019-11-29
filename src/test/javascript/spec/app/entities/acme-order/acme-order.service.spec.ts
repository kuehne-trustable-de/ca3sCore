import { TestBed, getTestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { take, map } from 'rxjs/operators';
import * as moment from 'moment';
import { DATE_FORMAT } from 'app/shared/constants/input.constants';
import { AcmeOrderService } from 'app/entities/acme-order/acme-order.service';
import { IAcmeOrder, AcmeOrder } from 'app/shared/model/acme-order.model';
import { OrderStatus } from 'app/shared/model/enumerations/order-status.model';

describe('Service Tests', () => {
  describe('AcmeOrder Service', () => {
    let injector: TestBed;
    let service: AcmeOrderService;
    let httpMock: HttpTestingController;
    let elemDefault: IAcmeOrder;
    let expectedResult;
    let currentDate: moment.Moment;
    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule]
      });
      expectedResult = {};
      injector = getTestBed();
      service = injector.get(AcmeOrderService);
      httpMock = injector.get(HttpTestingController);
      currentDate = moment();

      elemDefault = new AcmeOrder(0, 0, OrderStatus.Pending, currentDate, currentDate, currentDate, 'AAAAAAA', 'AAAAAAA', 'AAAAAAA');
    });

    describe('Service methods', () => {
      it('should find an element', () => {
        const returnedFromService = Object.assign(
          {
            expires: currentDate.format(DATE_FORMAT),
            notBefore: currentDate.format(DATE_FORMAT),
            notAfter: currentDate.format(DATE_FORMAT)
          },
          elemDefault
        );
        service
          .find(123)
          .pipe(take(1))
          .subscribe(resp => (expectedResult = resp));

        const req = httpMock.expectOne({ method: 'GET' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject({ body: elemDefault });
      });

      it('should create a AcmeOrder', () => {
        const returnedFromService = Object.assign(
          {
            id: 0,
            expires: currentDate.format(DATE_FORMAT),
            notBefore: currentDate.format(DATE_FORMAT),
            notAfter: currentDate.format(DATE_FORMAT)
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
        service
          .create(new AcmeOrder(null))
          .pipe(take(1))
          .subscribe(resp => (expectedResult = resp));
        const req = httpMock.expectOne({ method: 'POST' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject({ body: expected });
      });

      it('should update a AcmeOrder', () => {
        const returnedFromService = Object.assign(
          {
            orderId: 1,
            status: 'BBBBBB',
            expires: currentDate.format(DATE_FORMAT),
            notBefore: currentDate.format(DATE_FORMAT),
            notAfter: currentDate.format(DATE_FORMAT),
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
        service
          .update(expected)
          .pipe(take(1))
          .subscribe(resp => (expectedResult = resp));
        const req = httpMock.expectOne({ method: 'PUT' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject({ body: expected });
      });

      it('should return a list of AcmeOrder', () => {
        const returnedFromService = Object.assign(
          {
            orderId: 1,
            status: 'BBBBBB',
            expires: currentDate.format(DATE_FORMAT),
            notBefore: currentDate.format(DATE_FORMAT),
            notAfter: currentDate.format(DATE_FORMAT),
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
        service
          .query(expected)
          .pipe(
            take(1),
            map(resp => resp.body)
          )
          .subscribe(body => (expectedResult = body));
        const req = httpMock.expectOne({ method: 'GET' });
        req.flush([returnedFromService]);
        httpMock.verify();
        expect(expectedResult).toContainEqual(expected);
      });

      it('should delete a AcmeOrder', () => {
        service.delete(123).subscribe(resp => (expectedResult = resp.ok));

        const req = httpMock.expectOne({ method: 'DELETE' });
        req.flush({ status: 200 });
        expect(expectedResult);
      });
    });

    afterEach(() => {
      httpMock.verify();
    });
  });
});
