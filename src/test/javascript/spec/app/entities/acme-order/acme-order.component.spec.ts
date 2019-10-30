import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { HttpHeaders, HttpResponse } from '@angular/common/http';

import { Ca3SJhTestModule } from '../../../test.module';
import { AcmeOrderComponent } from 'app/entities/acme-order/acme-order.component';
import { AcmeOrderService } from 'app/entities/acme-order/acme-order.service';
import { AcmeOrder } from 'app/shared/model/acme-order.model';

describe('Component Tests', () => {
  describe('AcmeOrder Management Component', () => {
    let comp: AcmeOrderComponent;
    let fixture: ComponentFixture<AcmeOrderComponent>;
    let service: AcmeOrderService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [AcmeOrderComponent],
        providers: []
      })
        .overrideTemplate(AcmeOrderComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(AcmeOrderComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(AcmeOrderService);
    });

    it('Should call load all on init', () => {
      // GIVEN
      const headers = new HttpHeaders().append('link', 'link;link');
      spyOn(service, 'query').and.returnValue(
        of(
          new HttpResponse({
            body: [new AcmeOrder(123)],
            headers
          })
        )
      );

      // WHEN
      comp.ngOnInit();

      // THEN
      expect(service.query).toHaveBeenCalled();
      expect(comp.acmeOrders[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
  });
});
