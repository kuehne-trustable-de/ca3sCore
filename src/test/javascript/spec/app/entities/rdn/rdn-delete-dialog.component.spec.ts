import { ComponentFixture, TestBed, inject, fakeAsync, tick } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { of } from 'rxjs';
import { JhiEventManager } from 'ng-jhipster';

import { Ca3SJhTestModule } from '../../../test.module';
import { RDNDeleteDialogComponent } from 'app/entities/rdn/rdn-delete-dialog.component';
import { RDNService } from 'app/entities/rdn/rdn.service';

describe('Component Tests', () => {
  describe('RDN Management Delete Component', () => {
    let comp: RDNDeleteDialogComponent;
    let fixture: ComponentFixture<RDNDeleteDialogComponent>;
    let service: RDNService;
    let mockEventManager: any;
    let mockActiveModal: any;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [RDNDeleteDialogComponent]
      })
        .overrideTemplate(RDNDeleteDialogComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(RDNDeleteDialogComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(RDNService);
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
