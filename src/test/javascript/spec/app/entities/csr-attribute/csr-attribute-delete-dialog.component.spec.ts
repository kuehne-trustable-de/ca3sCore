import { ComponentFixture, TestBed, inject, fakeAsync, tick } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { of } from 'rxjs';
import { JhiEventManager } from 'ng-jhipster';

import { Ca3SJhTestModule } from '../../../test.module';
import { CsrAttributeDeleteDialogComponent } from 'app/entities/csr-attribute/csr-attribute-delete-dialog.component';
import { CsrAttributeService } from 'app/entities/csr-attribute/csr-attribute.service';

describe('Component Tests', () => {
  describe('CsrAttribute Management Delete Component', () => {
    let comp: CsrAttributeDeleteDialogComponent;
    let fixture: ComponentFixture<CsrAttributeDeleteDialogComponent>;
    let service: CsrAttributeService;
    let mockEventManager: any;
    let mockActiveModal: any;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [CsrAttributeDeleteDialogComponent]
      })
        .overrideTemplate(CsrAttributeDeleteDialogComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(CsrAttributeDeleteDialogComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(CsrAttributeService);
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
