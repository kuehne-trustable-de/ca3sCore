import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { HttpHeaders, HttpResponse } from '@angular/common/http';

import { Ca3SJhTestModule } from '../../../test.module';
import { CsrAttributeComponent } from 'app/entities/csr-attribute/csr-attribute.component';
import { CsrAttributeService } from 'app/entities/csr-attribute/csr-attribute.service';
import { CsrAttribute } from 'app/shared/model/csr-attribute.model';

describe('Component Tests', () => {
  describe('CsrAttribute Management Component', () => {
    let comp: CsrAttributeComponent;
    let fixture: ComponentFixture<CsrAttributeComponent>;
    let service: CsrAttributeService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [CsrAttributeComponent],
        providers: []
      })
        .overrideTemplate(CsrAttributeComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(CsrAttributeComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(CsrAttributeService);
    });

    it('Should call load all on init', () => {
      // GIVEN
      const headers = new HttpHeaders().append('link', 'link;link');
      spyOn(service, 'query').and.returnValue(
        of(
          new HttpResponse({
            body: [new CsrAttribute(123)],
            headers
          })
        )
      );

      // WHEN
      comp.ngOnInit();

      // THEN
      expect(service.query).toHaveBeenCalled();
      expect(comp.csrAttributes[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
  });
});
