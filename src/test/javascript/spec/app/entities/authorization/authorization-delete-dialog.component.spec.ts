import { ComponentFixture, TestBed, inject, fakeAsync, tick } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { of } from 'rxjs';
import { JhiEventManager } from 'ng-jhipster';

import { Ca3SJhTestModule } from '../../../test.module';
import { AuthorizationDeleteDialogComponent } from 'app/entities/authorization/authorization-delete-dialog.component';
import { AuthorizationService } from 'app/entities/authorization/authorization.service';

describe('Component Tests', () => {
  describe('Authorization Management Delete Component', () => {
    let comp: AuthorizationDeleteDialogComponent;
    let fixture: ComponentFixture<AuthorizationDeleteDialogComponent>;
    let service: AuthorizationService;
    let mockEventManager: any;
    let mockActiveModal: any;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [AuthorizationDeleteDialogComponent]
      })
        .overrideTemplate(AuthorizationDeleteDialogComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(AuthorizationDeleteDialogComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(AuthorizationService);
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
