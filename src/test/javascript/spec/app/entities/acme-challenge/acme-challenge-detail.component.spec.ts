import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { AcmeChallengeDetailComponent } from 'app/entities/acme-challenge/acme-challenge-detail.component';
import { AcmeChallenge } from 'app/shared/model/acme-challenge.model';

describe('Component Tests', () => {
  describe('AcmeChallenge Management Detail Component', () => {
    let comp: AcmeChallengeDetailComponent;
    let fixture: ComponentFixture<AcmeChallengeDetailComponent>;
    const route = ({ data: of({ acmeChallenge: new AcmeChallenge(123) }) } as any) as ActivatedRoute;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [AcmeChallengeDetailComponent],
        providers: [{ provide: ActivatedRoute, useValue: route }]
      })
        .overrideTemplate(AcmeChallengeDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(AcmeChallengeDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should call load all on init', () => {
        // GIVEN

        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.acmeChallenge).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
