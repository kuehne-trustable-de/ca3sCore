import { ComponentFixture, TestBed, inject, fakeAsync, tick } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { of } from 'rxjs';
import { JhiEventManager } from 'ng-jhipster';

import { Ca3SJhTestModule } from '../../../test.module';
import { CertificateAttributeDeleteDialogComponent } from 'app/entities/certificate-attribute/certificate-attribute-delete-dialog.component';
import { CertificateAttributeService } from 'app/entities/certificate-attribute/certificate-attribute.service';

describe('Component Tests', () => {
  describe('CertificateAttribute Management Delete Component', () => {
    let comp: CertificateAttributeDeleteDialogComponent;
    let fixture: ComponentFixture<CertificateAttributeDeleteDialogComponent>;
    let service: CertificateAttributeService;
    let mockEventManager: any;
    let mockActiveModal: any;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [CertificateAttributeDeleteDialogComponent]
      })
        .overrideTemplate(CertificateAttributeDeleteDialogComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(CertificateAttributeDeleteDialogComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(CertificateAttributeService);
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
