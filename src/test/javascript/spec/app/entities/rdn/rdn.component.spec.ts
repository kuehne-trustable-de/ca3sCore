import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { HttpHeaders, HttpResponse } from '@angular/common/http';

import { Ca3SJhTestModule } from '../../../test.module';
import { RDNComponent } from 'app/entities/rdn/rdn.component';
import { RDNService } from 'app/entities/rdn/rdn.service';
import { RDN } from 'app/shared/model/rdn.model';

describe('Component Tests', () => {
  describe('RDN Management Component', () => {
    let comp: RDNComponent;
    let fixture: ComponentFixture<RDNComponent>;
    let service: RDNService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [RDNComponent],
        providers: []
      })
        .overrideTemplate(RDNComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(RDNComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(RDNService);
    });

    it('Should call load all on init', () => {
      // GIVEN
      const headers = new HttpHeaders().append('link', 'link;link');
      spyOn(service, 'query').and.returnValue(
        of(
          new HttpResponse({
            body: [new RDN(123)],
            headers
          })
        )
      );

      // WHEN
      comp.ngOnInit();

      // THEN
      expect(service.query).toHaveBeenCalled();
      expect(comp.rDNS[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
  });
});
