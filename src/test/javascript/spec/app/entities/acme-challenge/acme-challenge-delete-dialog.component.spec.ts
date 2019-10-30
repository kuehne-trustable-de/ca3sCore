import { ComponentFixture, TestBed, inject, fakeAsync, tick } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { of } from 'rxjs';
import { JhiEventManager } from 'ng-jhipster';

import { Ca3SJhTestModule } from '../../../test.module';
import { AcmeChallengeDeleteDialogComponent } from 'app/entities/acme-challenge/acme-challenge-delete-dialog.component';
import { AcmeChallengeService } from 'app/entities/acme-challenge/acme-challenge.service';

describe('Component Tests', () => {
  describe('AcmeChallenge Management Delete Component', () => {
    let comp: AcmeChallengeDeleteDialogComponent;
    let fixture: ComponentFixture<AcmeChallengeDeleteDialogComponent>;
    let service: AcmeChallengeService;
    let mockEventManager: any;
    let mockActiveModal: any;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [AcmeChallengeDeleteDialogComponent]
      })
        .overrideTemplate(AcmeChallengeDeleteDialogComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(AcmeChallengeDeleteDialogComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(AcmeChallengeService);
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
