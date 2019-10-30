import { ComponentFixture, TestBed, inject, fakeAsync, tick } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { of } from 'rxjs';
import { JhiEventManager } from 'ng-jhipster';

import { Ca3SJhTestModule } from '../../../test.module';
import { SelectorToTemplateDeleteDialogComponent } from 'app/entities/selector-to-template/selector-to-template-delete-dialog.component';
import { SelectorToTemplateService } from 'app/entities/selector-to-template/selector-to-template.service';

describe('Component Tests', () => {
  describe('SelectorToTemplate Management Delete Component', () => {
    let comp: SelectorToTemplateDeleteDialogComponent;
    let fixture: ComponentFixture<SelectorToTemplateDeleteDialogComponent>;
    let service: SelectorToTemplateService;
    let mockEventManager: any;
    let mockActiveModal: any;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [SelectorToTemplateDeleteDialogComponent]
      })
        .overrideTemplate(SelectorToTemplateDeleteDialogComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(SelectorToTemplateDeleteDialogComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(SelectorToTemplateService);
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
