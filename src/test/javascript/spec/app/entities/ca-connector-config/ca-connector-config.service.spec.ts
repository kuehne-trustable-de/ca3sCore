import { TestBed, getTestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { take, map } from 'rxjs/operators';
import { CAConnectorConfigService } from 'app/entities/ca-connector-config/ca-connector-config.service';
import { ICAConnectorConfig, CAConnectorConfig } from 'app/shared/model/ca-connector-config.model';
import { CAConnectorType } from 'app/shared/model/enumerations/ca-connector-type.model';

describe('Service Tests', () => {
  describe('CAConnectorConfig Service', () => {
    let injector: TestBed;
    let service: CAConnectorConfigService;
    let httpMock: HttpTestingController;
    let elemDefault: ICAConnectorConfig;
    let expectedResult;
    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [HttpClientTestingModule]
      });
      expectedResult = {};
      injector = getTestBed();
      service = injector.get(CAConnectorConfigService);
      httpMock = injector.get(HttpTestingController);

      elemDefault = new CAConnectorConfig(0, 0, 'AAAAAAA', CAConnectorType.Internal, 'AAAAAAA', 'AAAAAAA', 0, false);
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

      it('should create a CAConnectorConfig', () => {
        const returnedFromService = Object.assign(
          {
            id: 0
          },
          elemDefault
        );
        const expected = Object.assign({}, returnedFromService);
        service
          .create(new CAConnectorConfig(null))
          .pipe(take(1))
          .subscribe(resp => (expectedResult = resp));
        const req = httpMock.expectOne({ method: 'POST' });
        req.flush(returnedFromService);
        expect(expectedResult).toMatchObject({ body: expected });
      });

      it('should update a CAConnectorConfig', () => {
        const returnedFromService = Object.assign(
          {
            configId: 1,
            name: 'BBBBBB',
            caConnectorType: 'BBBBBB',
            caUrl: 'BBBBBB',
            secret: 'BBBBBB',
            pollingOffset: 1,
            active: true
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

      it('should return a list of CAConnectorConfig', () => {
        const returnedFromService = Object.assign(
          {
            configId: 1,
            name: 'BBBBBB',
            caConnectorType: 'BBBBBB',
            caUrl: 'BBBBBB',
            secret: 'BBBBBB',
            pollingOffset: 1,
            active: true
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

      it('should delete a CAConnectorConfig', () => {
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
