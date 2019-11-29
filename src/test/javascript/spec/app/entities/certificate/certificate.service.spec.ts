import { TestBed, getTestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { take, map } from 'rxjs/operators';
import * as moment from 'moment';
import { DATE_FORMAT } from 'app/shared/constants/input.constants';
import { CertificateService } from 'app/entities/certificate/certificate.service';
import { ICertificate, Certificate } from 'app/shared/model/certificate.model';

describe('Service Tests', () => {
  describe('Certificate Service', () => {
    let injector: TestBed;
    let service: CertificateService;
    let httpMock: HttpTestingController;
    let elemDefault: ICertificate;
    let expectedResult;
    let currentDate: moment.Moment;
    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule]
      });
      expectedResult = {};
      injector = getTestBed();
      service = injector.get(CertificateService);
      httpMock = injector.get(HttpTestingController);
      currentDate = moment();

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
        currentDate,
        currentDate,
        'AAAAAAA',
        false,
        'AAAAAAA',
        'AAAAAAA'
      );
    });

    describe('Service methods', () => {
      it('should find an element', () => {
        const returnedFromService = Object.assign(
          {
            validFrom: currentDate.format(DATE_FORMAT),
            validTo: currentDate.format(DATE_FORMAT),
            contentAddedAt: currentDate.format(DATE_FORMAT),
            revokedSince: currentDate.format(DATE_FORMAT)
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

      it('should create a Certificate', () => {
        const returnedFromService = Object.assign(
          {
            id: 0,
            validFrom: currentDate.format(DATE_FORMAT),
            validTo: currentDate.format(DATE_FORMAT),
            contentAddedAt: currentDate.format(DATE_FORMAT),
            revokedSince: currentDate.format(DATE_FORMAT)
          },
          elemDefault
        );
        const expected = Object.assign(
          {
            validFrom: currentDate,
            validTo: currentDate,
            contentAddedAt: currentDate,
            revokedSince: currentDate
          },
          returnedFromService
        );
        service
          .create(new Certificate(null))
          .pipe(take(1))
          .subscribe(resp => (expectedResult = resp));
        const req = httpMock.expectOne({ method: 'POST' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject({ body: expected });
      });

      it('should update a Certificate', () => {
        const returnedFromService = Object.assign(
          {
            tbsDigest: 'BBBBBB',
            subject: 'BBBBBB',
            issuer: 'BBBBBB',
            type: 'BBBBBB',
            description: 'BBBBBB',
            subjectKeyIdentifier: 'BBBBBB',
            authorityKeyIdentifier: 'BBBBBB',
            fingerprint: 'BBBBBB',
            serial: 'BBBBBB',
            validFrom: currentDate.format(DATE_FORMAT),
            validTo: currentDate.format(DATE_FORMAT),
            creationExecutionId: 'BBBBBB',
            contentAddedAt: currentDate.format(DATE_FORMAT),
            revokedSince: currentDate.format(DATE_FORMAT),
            revocationReason: 'BBBBBB',
            revoked: true,
            revocationExecutionId: 'BBBBBB',
            content: 'BBBBBB'
          },
          elemDefault
        );

        const expected = Object.assign(
          {
            validFrom: currentDate,
            validTo: currentDate,
            contentAddedAt: currentDate,
            revokedSince: currentDate
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

      it('should return a list of Certificate', () => {
        const returnedFromService = Object.assign(
          {
            tbsDigest: 'BBBBBB',
            subject: 'BBBBBB',
            issuer: 'BBBBBB',
            type: 'BBBBBB',
            description: 'BBBBBB',
            subjectKeyIdentifier: 'BBBBBB',
            authorityKeyIdentifier: 'BBBBBB',
            fingerprint: 'BBBBBB',
            serial: 'BBBBBB',
            validFrom: currentDate.format(DATE_FORMAT),
            validTo: currentDate.format(DATE_FORMAT),
            creationExecutionId: 'BBBBBB',
            contentAddedAt: currentDate.format(DATE_FORMAT),
            revokedSince: currentDate.format(DATE_FORMAT),
            revocationReason: 'BBBBBB',
            revoked: true,
            revocationExecutionId: 'BBBBBB',
            content: 'BBBBBB'
          },
          elemDefault
        );
        const expected = Object.assign(
          {
            validFrom: currentDate,
            validTo: currentDate,
            contentAddedAt: currentDate,
            revokedSince: currentDate
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

      it('should delete a Certificate', () => {
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
