import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { HttpHeaders, HttpResponse } from '@angular/common/http';

import { Ca3SJhTestModule } from '../../../test.module';
import { RDNAttributeComponent } from 'app/entities/rdn-attribute/rdn-attribute.component';
import { RDNAttributeService } from 'app/entities/rdn-attribute/rdn-attribute.service';
import { RDNAttribute } from 'app/shared/model/rdn-attribute.model';

describe('Component Tests', () => {
  describe('RDNAttribute Management Component', () => {
    let comp: RDNAttributeComponent;
    let fixture: ComponentFixture<RDNAttributeComponent>;
    let service: RDNAttributeService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [RDNAttributeComponent],
        providers: []
      })
        .overrideTemplate(RDNAttributeComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(RDNAttributeComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(RDNAttributeService);
    });

    it('Should call load all on init', () => {
      // GIVEN
      const headers = new HttpHeaders().append('link', 'link;link');
      spyOn(service, 'query').and.returnValue(
        of(
          new HttpResponse({
            body: [new RDNAttribute(123)],
            headers
          })
        )
      );

      // WHEN
      comp.ngOnInit();

      // THEN
      expect(service.query).toHaveBeenCalled();
      expect(comp.rDNAttributes[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
  });
});
