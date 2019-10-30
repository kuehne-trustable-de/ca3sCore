import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { HttpHeaders, HttpResponse } from '@angular/common/http';

import { Ca3SJhTestModule } from '../../../test.module';
import { CSRComponent } from 'app/entities/csr/csr.component';
import { CSRService } from 'app/entities/csr/csr.service';
import { CSR } from 'app/shared/model/csr.model';

describe('Component Tests', () => {
  describe('CSR Management Component', () => {
    let comp: CSRComponent;
    let fixture: ComponentFixture<CSRComponent>;
    let service: CSRService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [CSRComponent],
        providers: []
      })
        .overrideTemplate(CSRComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(CSRComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(CSRService);
    });

    it('Should call load all on init', () => {
      // GIVEN
      const headers = new HttpHeaders().append('link', 'link;link');
      spyOn(service, 'query').and.returnValue(
        of(
          new HttpResponse({
            body: [new CSR(123)],
            headers
          })
        )
      );

      // WHEN
      comp.ngOnInit();

      // THEN
      expect(service.query).toHaveBeenCalled();
      expect(comp.cSRS[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
  });
});
