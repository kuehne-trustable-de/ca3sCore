import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { AcmeOrderDetailComponent } from 'app/entities/acme-order/acme-order-detail.component';
import { AcmeOrder } from 'app/shared/model/acme-order.model';

describe('Component Tests', () => {
  describe('AcmeOrder Management Detail Component', () => {
    let comp: AcmeOrderDetailComponent;
    let fixture: ComponentFixture<AcmeOrderDetailComponent>;
    const route = ({ data: of({ acmeOrder: new AcmeOrder(123) }) } as any) as ActivatedRoute;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [AcmeOrderDetailComponent],
        providers: [{ provide: ActivatedRoute, useValue: route }]
      })
        .overrideTemplate(AcmeOrderDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(AcmeOrderDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should call load all on init', () => {
        // GIVEN

        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.acmeOrder).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
