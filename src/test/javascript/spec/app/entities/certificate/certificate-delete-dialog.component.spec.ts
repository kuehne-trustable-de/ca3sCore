import { ComponentFixture, TestBed, inject, fakeAsync, tick } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { of } from 'rxjs';
import { JhiEventManager } from 'ng-jhipster';

import { Ca3SJhTestModule } from '../../../test.module';
import { CertificateDeleteDialogComponent } from 'app/entities/certificate/certificate-delete-dialog.component';
import { CertificateService } from 'app/entities/certificate/certificate.service';

describe('Component Tests', () => {
  describe('Certificate Management Delete Component', () => {
    let comp: CertificateDeleteDialogComponent;
    let fixture: ComponentFixture<CertificateDeleteDialogComponent>;
    let service: CertificateService;
    let mockEventManager: any;
    let mockActiveModal: any;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [CertificateDeleteDialogComponent]
      })
        .overrideTemplate(CertificateDeleteDialogComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(CertificateDeleteDialogComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(CertificateService);
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
