import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { HttpHeaders, HttpResponse } from '@angular/common/http';

import { Ca3SJhTestModule } from '../../../test.module';
import { AcmeChallengeComponent } from 'app/entities/acme-challenge/acme-challenge.component';
import { AcmeChallengeService } from 'app/entities/acme-challenge/acme-challenge.service';
import { AcmeChallenge } from 'app/shared/model/acme-challenge.model';

describe('Component Tests', () => {
  describe('AcmeChallenge Management Component', () => {
    let comp: AcmeChallengeComponent;
    let fixture: ComponentFixture<AcmeChallengeComponent>;
    let service: AcmeChallengeService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [AcmeChallengeComponent],
        providers: []
      })
        .overrideTemplate(AcmeChallengeComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(AcmeChallengeComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(AcmeChallengeService);
    });

    it('Should call load all on init', () => {
      // GIVEN
      const headers = new HttpHeaders().append('link', 'link;link');
      spyOn(service, 'query').and.returnValue(
        of(
          new HttpResponse({
            body: [new AcmeChallenge(123)],
            headers
          })
        )
      );

      // WHEN
      comp.ngOnInit();

      // THEN
      expect(service.query).toHaveBeenCalled();
      expect(comp.acmeChallenges[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
  });
});
