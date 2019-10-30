import { TestBed, getTestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { take, map } from 'rxjs/operators';
import { ACMEAccountService } from 'app/entities/acme-account/acme-account.service';
import { IACMEAccount, ACMEAccount } from 'app/shared/model/acme-account.model';
import { AccountStatus } from 'app/shared/model/enumerations/account-status.model';

describe('Service Tests', () => {
  describe('ACMEAccount Service', () => {
    let injector: TestBed;
    let service: ACMEAccountService;
    let httpMock: HttpTestingController;
    let elemDefault: IACMEAccount;
    let expectedResult;
    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule]
      });
      expectedResult = {};
      injector = getTestBed();
      service = injector.get(ACMEAccountService);
      httpMock = injector.get(HttpTestingController);

      elemDefault = new ACMEAccount(0, 0, 'AAAAAAA', AccountStatus.Valid, false, 'AAAAAAA', 'AAAAAAA');
    });

    describe('Service methods', () => {
      it('should find an element', () => {
        const returnedFromService = Object.assign({}, elemDefault);
        service
          .find(123)
          .pipe(take(1))
          .subscribe(resp => (expectedResult = resp));

        const req = httpMock.expectOne({ method: 'GET' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject({ body: elemDefault });
      });

      it('should create a ACMEAccount', () => {
        const returnedFromService = Object.assign(
          {
            id: 0
          },
          elemDefault
        );
        const expected = Object.assign({}, returnedFromService);
        service
          .create(new ACMEAccount(null))
          .pipe(take(1))
          .subscribe(resp => (expectedResult = resp));
        const req = httpMock.expectOne({ method: 'POST' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject({ body: expected });
      });

      it('should update a ACMEAccount', () => {
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
        service
          .update(expected)
          .pipe(take(1))
          .subscribe(resp => (expectedResult = resp));
        const req = httpMock.expectOne({ method: 'PUT' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject({ body: expected });
      });

      it('should return a list of ACMEAccount', () => {
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

      it('should delete a ACMEAccount', () => {
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
