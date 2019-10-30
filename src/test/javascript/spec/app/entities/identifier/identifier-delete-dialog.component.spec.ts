import { ComponentFixture, TestBed, inject, fakeAsync, tick } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { of } from 'rxjs';
import { JhiEventManager } from 'ng-jhipster';

import { Ca3SJhTestModule } from '../../../test.module';
import { IdentifierDeleteDialogComponent } from 'app/entities/identifier/identifier-delete-dialog.component';
import { IdentifierService } from 'app/entities/identifier/identifier.service';

describe('Component Tests', () => {
  describe('Identifier Management Delete Component', () => {
    let comp: IdentifierDeleteDialogComponent;
    let fixture: ComponentFixture<IdentifierDeleteDialogComponent>;
    let service: IdentifierService;
    let mockEventManager: any;
    let mockActiveModal: any;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [IdentifierDeleteDialogComponent]
      })
        .overrideTemplate(IdentifierDeleteDialogComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(IdentifierDeleteDialogComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(IdentifierService);
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
