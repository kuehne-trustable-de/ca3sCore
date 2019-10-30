import { ComponentFixture, TestBed, inject, fakeAsync, tick } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { of } from 'rxjs';
import { JhiEventManager } from 'ng-jhipster';

import { Ca3SJhTestModule } from '../../../test.module';
import { AcmeOrderDeleteDialogComponent } from 'app/entities/acme-order/acme-order-delete-dialog.component';
import { AcmeOrderService } from 'app/entities/acme-order/acme-order.service';

describe('Component Tests', () => {
  describe('AcmeOrder Management Delete Component', () => {
    let comp: AcmeOrderDeleteDialogComponent;
    let fixture: ComponentFixture<AcmeOrderDeleteDialogComponent>;
    let service: AcmeOrderService;
    let mockEventManager: any;
    let mockActiveModal: any;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [AcmeOrderDeleteDialogComponent]
      })
        .overrideTemplate(AcmeOrderDeleteDialogComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(AcmeOrderDeleteDialogComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(AcmeOrderService);
      mockEventManager = fixture.debugElement.injector.get(JhiEventManager);
      mockActiveModal = fixture.debugElement.injector.get(NgbActiveModal);
    });

    describe('confirmDelete', () => {
      it('Should call delete service on confirmDelete', inject(
        [],
        fakeAsync(() => {
          // GIVEN
          spyOn(service, 'delete').and.returnValue(of({}));

          // WHEN
          comp.confirmDelete(123);
          tick();

          // THEN
          expect(service.delete).toHaveBeenCalledWith(123);
          expect(mockActiveModal.dismissSpy).toHaveBeenCalled();
          expect(mockEventManager.broadcastSpy).toHaveBeenCalled();
        })
      ));
    });
  });
});
