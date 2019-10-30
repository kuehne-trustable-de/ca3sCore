import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { HttpHeaders, HttpResponse } from '@angular/common/http';

import { Ca3SJhTestModule } from '../../../test.module';
import { AcmeContactComponent } from 'app/entities/acme-contact/acme-contact.component';
import { AcmeContactService } from 'app/entities/acme-contact/acme-contact.service';
import { AcmeContact } from 'app/shared/model/acme-contact.model';

describe('Component Tests', () => {
  describe('AcmeContact Management Component', () => {
    let comp: AcmeContactComponent;
    let fixture: ComponentFixture<AcmeContactComponent>;
    let service: AcmeContactService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [AcmeContactComponent],
        providers: []
      })
        .overrideTemplate(AcmeContactComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(AcmeContactComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(AcmeContactService);
    });

    it('Should call load all on init', () => {
      // GIVEN
      const headers = new HttpHeaders().append('link', 'link;link');
      spyOn(service, 'query').and.returnValue(
        of(
          new HttpResponse({
            body: [new AcmeContact(123)],
            headers
          })
        )
      );

      // WHEN
      comp.ngOnInit();

      // THEN
      expect(service.query).toHaveBeenCalled();
      expect(comp.acmeContacts[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
  });
});
