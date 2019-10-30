import { ComponentFixture, TestBed, inject, fakeAsync, tick } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { of } from 'rxjs';
import { JhiEventManager } from 'ng-jhipster';

import { Ca3SJhTestModule } from '../../../test.module';
import { RDNAttributeDeleteDialogComponent } from 'app/entities/rdn-attribute/rdn-attribute-delete-dialog.component';
import { RDNAttributeService } from 'app/entities/rdn-attribute/rdn-attribute.service';

describe('Component Tests', () => {
  describe('RDNAttribute Management Delete Component', () => {
    let comp: RDNAttributeDeleteDialogComponent;
    let fixture: ComponentFixture<RDNAttributeDeleteDialogComponent>;
    let service: RDNAttributeService;
    let mockEventManager: any;
    let mockActiveModal: any;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [RDNAttributeDeleteDialogComponent]
      })
        .overrideTemplate(RDNAttributeDeleteDialogComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(RDNAttributeDeleteDialogComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(RDNAttributeService);
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
