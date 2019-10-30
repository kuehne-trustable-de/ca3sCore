import { ComponentFixture, TestBed, inject, fakeAsync, tick } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { of } from 'rxjs';
import { JhiEventManager } from 'ng-jhipster';

import { Ca3SJhTestModule } from '../../../test.module';
import { ACMEAccountDeleteDialogComponent } from 'app/entities/acme-account/acme-account-delete-dialog.component';
import { ACMEAccountService } from 'app/entities/acme-account/acme-account.service';

describe('Component Tests', () => {
  describe('ACMEAccount Management Delete Component', () => {
    let comp: ACMEAccountDeleteDialogComponent;
    let fixture: ComponentFixture<ACMEAccountDeleteDialogComponent>;
    let service: ACMEAccountService;
    let mockEventManager: any;
    let mockActiveModal: any;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [ACMEAccountDeleteDialogComponent]
      })
        .overrideTemplate(ACMEAccountDeleteDialogComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(ACMEAccountDeleteDialogComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(ACMEAccountService);
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
