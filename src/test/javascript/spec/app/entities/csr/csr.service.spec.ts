import { TestBed, getTestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { take, map } from 'rxjs/operators';
import * as moment from 'moment';
import { DATE_FORMAT } from 'app/shared/constants/input.constants';
import { CSRService } from 'app/entities/csr/csr.service';
import { ICSR, CSR } from 'app/shared/model/csr.model';

describe('Service Tests', () => {
  describe('CSR Service', () => {
    let injector: TestBed;
    let service: CSRService;
    let httpMock: HttpTestingController;
    let elemDefault: ICSR;
    let expectedResult;
    let currentDate: moment.Moment;
    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule]
      });
      expectedResult = {};
      injector = getTestBed();
      service = injector.get(CSRService);
      httpMock = injector.get(HttpTestingController);
      currentDate = moment();

      elemDefault = new CSR(0, 'AAAAAAA', currentDate, 'AAAAAAA', 'AAAAAAA', 'AAAAAAA', false, 'AAAAAAA', 'AAAAAAA', 'AAAAAAA', 'AAAAAAA');
    });

    describe('Service methods', () => {
      it('should find an element', () => {
        const returnedFromService = Object.assign(
          {
            requestedOn: currentDate.format(DATE_FORMAT)
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

      it('should create a CSR', () => {
        const returnedFromService = Object.assign(
          {
            id: 0,
            requestedOn: currentDate.format(DATE_FORMAT)
          },
          elemDefault
        );
        const expected = Object.assign(
          {
            requestedOn: currentDate
          },
          returnedFromService
        );
        service
          .create(new CSR(null))
          .pipe(take(1))
          .subscribe(resp => (expectedResult = resp));
        const req = httpMock.expectOne({ method: 'POST' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject({ body: expected });
      });

      it('should update a CSR', () => {
        const returnedFromService = Object.assign(
          {
            csrBase64: 'BBBBBB',
            requestedOn: currentDate.format(DATE_FORMAT),
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
        service
          .update(expected)
          .pipe(take(1))
          .subscribe(resp => (expectedResult = resp));
        const req = httpMock.expectOne({ method: 'PUT' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject({ body: expected });
      });

      it('should return a list of CSR', () => {
        const returnedFromService = Object.assign(
          {
            csrBase64: 'BBBBBB',
            requestedOn: currentDate.format(DATE_FORMAT),
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

      it('should delete a CSR', () => {
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
