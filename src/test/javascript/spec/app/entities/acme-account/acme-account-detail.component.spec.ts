import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { ACMEAccountDetailComponent } from 'app/entities/acme-account/acme-account-detail.component';
import { ACMEAccount } from 'app/shared/model/acme-account.model';

describe('Component Tests', () => {
  describe('ACMEAccount Management Detail Component', () => {
    let comp: ACMEAccountDetailComponent;
    let fixture: ComponentFixture<ACMEAccountDetailComponent>;
    const route = ({ data: of({ aCMEAccount: new ACMEAccount(123) }) } as any) as ActivatedRoute;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [ACMEAccountDetailComponent],
        providers: [{ provide: ActivatedRoute, useValue: route }]
      })
        .overrideTemplate(ACMEAccountDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(ACMEAccountDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should call load all on init', () => {
        // GIVEN

        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.aCMEAccount).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
