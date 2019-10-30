import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { AcmeContactDetailComponent } from 'app/entities/acme-contact/acme-contact-detail.component';
import { AcmeContact } from 'app/shared/model/acme-contact.model';

describe('Component Tests', () => {
  describe('AcmeContact Management Detail Component', () => {
    let comp: AcmeContactDetailComponent;
    let fixture: ComponentFixture<AcmeContactDetailComponent>;
    const route = ({ data: of({ acmeContact: new AcmeContact(123) }) } as any) as ActivatedRoute;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [AcmeContactDetailComponent],
        providers: [{ provide: ActivatedRoute, useValue: route }]
      })
        .overrideTemplate(AcmeContactDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(AcmeContactDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should call load all on init', () => {
        // GIVEN

        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.acmeContact).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
