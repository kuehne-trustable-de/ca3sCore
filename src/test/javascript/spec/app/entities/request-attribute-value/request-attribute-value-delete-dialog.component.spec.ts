import { ComponentFixture, TestBed, inject, fakeAsync, tick } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { of } from 'rxjs';
import { JhiEventManager } from 'ng-jhipster';

import { Ca3SJhTestModule } from '../../../test.module';
import { RequestAttributeValueDeleteDialogComponent } from 'app/entities/request-attribute-value/request-attribute-value-delete-dialog.component';
import { RequestAttributeValueService } from 'app/entities/request-attribute-value/request-attribute-value.service';

describe('Component Tests', () => {
  describe('RequestAttributeValue Management Delete Component', () => {
    let comp: RequestAttributeValueDeleteDialogComponent;
    let fixture: ComponentFixture<RequestAttributeValueDeleteDialogComponent>;
    let service: RequestAttributeValueService;
    let mockEventManager: any;
    let mockActiveModal: any;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [RequestAttributeValueDeleteDialogComponent]
      })
        .overrideTemplate(RequestAttributeValueDeleteDialogComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(RequestAttributeValueDeleteDialogComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(RequestAttributeValueService);
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
