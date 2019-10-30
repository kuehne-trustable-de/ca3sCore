import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { AcmeOrderUpdateComponent } from 'app/entities/acme-order/acme-order-update.component';
import { AcmeOrderService } from 'app/entities/acme-order/acme-order.service';
import { AcmeOrder } from 'app/shared/model/acme-order.model';

describe('Component Tests', () => {
  describe('AcmeOrder Management Update Component', () => {
    let comp: AcmeOrderUpdateComponent;
    let fixture: ComponentFixture<AcmeOrderUpdateComponent>;
    let service: AcmeOrderService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [AcmeOrderUpdateComponent],
        providers: [FormBuilder]
      })
        .overrideTemplate(AcmeOrderUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(AcmeOrderUpdateComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(AcmeOrderService);
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', fakeAsync(() => {
        // GIVEN
        const entity = new AcmeOrder(123);
        spyOn(service, 'update').and.returnValue(of(new HttpResponse({ body: entity })));
        comp.updateForm(entity);
        // WHEN
        comp.save();
        tick(); // simulate async

        // THEN
        expect(service.update).toHaveBeenCalledWith(entity);
        expect(comp.isSaving).toEqual(false);
      }));

      it('Should call create service on save for new entity', fakeAsync(() => {
        // GIVEN
        const entity = new AcmeOrder();
        spyOn(service, 'create').and.returnValue(of(new HttpResponse({ body: entity })));
        comp.updateForm(entity);
        // WHEN
        comp.save();
        tick(); // simulate async

        // THEN
        expect(service.create).toHaveBeenCalledWith(entity);
        expect(comp.isSaving).toEqual(false);
      }));
    });
  });
});
