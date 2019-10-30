import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { HttpHeaders, HttpResponse } from '@angular/common/http';

import { Ca3SJhTestModule } from '../../../test.module';
import { CertificateAttributeComponent } from 'app/entities/certificate-attribute/certificate-attribute.component';
import { CertificateAttributeService } from 'app/entities/certificate-attribute/certificate-attribute.service';
import { CertificateAttribute } from 'app/shared/model/certificate-attribute.model';

describe('Component Tests', () => {
  describe('CertificateAttribute Management Component', () => {
    let comp: CertificateAttributeComponent;
    let fixture: ComponentFixture<CertificateAttributeComponent>;
    let service: CertificateAttributeService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [CertificateAttributeComponent],
        providers: []
      })
        .overrideTemplate(CertificateAttributeComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(CertificateAttributeComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(CertificateAttributeService);
    });

    it('Should call load all on init', () => {
      // GIVEN
      const headers = new HttpHeaders().append('link', 'link;link');
      spyOn(service, 'query').and.returnValue(
        of(
          new HttpResponse({
            body: [new CertificateAttribute(123)],
            headers
          })
        )
      );

      // WHEN
      comp.ngOnInit();

      // THEN
      expect(service.query).toHaveBeenCalled();
      expect(comp.certificateAttributes[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
  });
});
