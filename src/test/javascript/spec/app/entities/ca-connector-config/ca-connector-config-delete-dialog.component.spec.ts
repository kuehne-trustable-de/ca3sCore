import { ComponentFixture, TestBed, inject, fakeAsync, tick } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { of } from 'rxjs';
import { JhiEventManager } from 'ng-jhipster';

import { Ca3SJhTestModule } from '../../../test.module';
import { CAConnectorConfigDeleteDialogComponent } from 'app/entities/ca-connector-config/ca-connector-config-delete-dialog.component';
import { CAConnectorConfigService } from 'app/entities/ca-connector-config/ca-connector-config.service';

describe('Component Tests', () => {
  describe('CAConnectorConfig Management Delete Component', () => {
    let comp: CAConnectorConfigDeleteDialogComponent;
    let fixture: ComponentFixture<CAConnectorConfigDeleteDialogComponent>;
    let service: CAConnectorConfigService;
    let mockEventManager: any;
    let mockActiveModal: any;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [CAConnectorConfigDeleteDialogComponent]
      })
        .overrideTemplate(CAConnectorConfigDeleteDialogComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(CAConnectorConfigDeleteDialogComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(CAConnectorConfigService);
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
