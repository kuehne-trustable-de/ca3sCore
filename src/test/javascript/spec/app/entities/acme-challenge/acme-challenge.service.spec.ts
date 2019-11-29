import { TestBed, getTestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { take, map } from 'rxjs/operators';
import * as moment from 'moment';
import { DATE_FORMAT } from 'app/shared/constants/input.constants';
import { AcmeChallengeService } from 'app/entities/acme-challenge/acme-challenge.service';
import { IAcmeChallenge, AcmeChallenge } from 'app/shared/model/acme-challenge.model';
import { ChallengeStatus } from 'app/shared/model/enumerations/challenge-status.model';

describe('Service Tests', () => {
  describe('AcmeChallenge Service', () => {
    let injector: TestBed;
    let service: AcmeChallengeService;
    let httpMock: HttpTestingController;
    let elemDefault: IAcmeChallenge;
    let expectedResult;
    let currentDate: moment.Moment;
    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule]
      });
      expectedResult = {};
      injector = getTestBed();
      service = injector.get(AcmeChallengeService);
      httpMock = injector.get(HttpTestingController);
      currentDate = moment();

      elemDefault = new AcmeChallenge(0, 0, 'AAAAAAA', 'AAAAAAA', 'AAAAAAA', currentDate, ChallengeStatus.Pending);
    });

    describe('Service methods', () => {
      it('should find an element', () => {
        const returnedFromService = Object.assign(
          {
            validated: currentDate.format(DATE_FORMAT)
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

      it('should create a AcmeChallenge', () => {
        const returnedFromService = Object.assign(
          {
            id: 0,
            validated: currentDate.format(DATE_FORMAT)
          },
          elemDefault
        );
        const expected = Object.assign(
          {
            validated: currentDate
          },
          returnedFromService
        );
        service
          .create(new AcmeChallenge(null))
          .pipe(take(1))
          .subscribe(resp => (expectedResult = resp));
        const req = httpMock.expectOne({ method: 'POST' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject({ body: expected });
      });

      it('should update a AcmeChallenge', () => {
        const returnedFromService = Object.assign(
          {
            challengeId: 1,
            type: 'BBBBBB',
            value: 'BBBBBB',
            token: 'BBBBBB',
            validated: currentDate.format(DATE_FORMAT),
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
        service
          .update(expected)
          .pipe(take(1))
          .subscribe(resp => (expectedResult = resp));
        const req = httpMock.expectOne({ method: 'PUT' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject({ body: expected });
      });

      it('should return a list of AcmeChallenge', () => {
        const returnedFromService = Object.assign(
          {
            challengeId: 1,
            type: 'BBBBBB',
            value: 'BBBBBB',
            token: 'BBBBBB',
            validated: currentDate.format(DATE_FORMAT),
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

      it('should delete a AcmeChallenge', () => {
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
