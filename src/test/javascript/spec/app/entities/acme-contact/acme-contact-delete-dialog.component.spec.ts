import { ComponentFixture, TestBed, inject, fakeAsync, tick } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { of } from 'rxjs';
import { JhiEventManager } from 'ng-jhipster';

import { Ca3SJhTestModule } from '../../../test.module';
import { AcmeContactDeleteDialogComponent } from 'app/entities/acme-contact/acme-contact-delete-dialog.component';
import { AcmeContactService } from 'app/entities/acme-contact/acme-contact.service';

describe('Component Tests', () => {
  describe('AcmeContact Management Delete Component', () => {
    let comp: AcmeContactDeleteDialogComponent;
    let fixture: ComponentFixture<AcmeContactDeleteDialogComponent>;
    let service: AcmeContactService;
    let mockEventManager: any;
    let mockActiveModal: any;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [AcmeContactDeleteDialogComponent]
      })
        .overrideTemplate(AcmeContactDeleteDialogComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(AcmeContactDeleteDialogComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(AcmeContactService);
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
