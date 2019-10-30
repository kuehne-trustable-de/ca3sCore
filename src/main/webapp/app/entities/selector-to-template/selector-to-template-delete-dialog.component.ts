import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { ISelectorToTemplate } from 'app/shared/model/selector-to-template.model';
import { SelectorToTemplateService } from './selector-to-template.service';

@Component({
  selector: 'jhi-selector-to-template-delete-dialog',
  templateUrl: './selector-to-template-delete-dialog.component.html'
})
export class SelectorToTemplateDeleteDialogComponent {
  selectorToTemplate: ISelectorToTemplate;

  constructor(
    protected selectorToTemplateService: SelectorToTemplateService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  clear() {
    this.activeModal.dismiss('cancel');
  }

  confirmDelete(id: number) {
    this.selectorToTemplateService.delete(id).subscribe(response => {
      this.eventManager.broadcast({
        name: 'selectorToTemplateListModification',
        content: 'Deleted an selectorToTemplate'
      });
      this.activeModal.dismiss(true);
    });
  }
}

@Component({
  selector: 'jhi-selector-to-template-delete-popup',
  template: ''
})
export class SelectorToTemplateDeletePopupComponent implements OnInit, OnDestroy {
  protected ngbModalRef: NgbModalRef;

  constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ selectorToTemplate }) => {
      setTimeout(() => {
        this.ngbModalRef = this.modalService.open(SelectorToTemplateDeleteDialogComponent as Component, { size: 'lg', backdrop: 'static' });
        this.ngbModalRef.componentInstance.selectorToTemplate = selectorToTemplate;
        this.ngbModalRef.result.then(
          result => {
            this.router.navigate(['/selector-to-template', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          },
          reason => {
            this.router.navigate(['/selector-to-template', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          }
        );
      }, 0);
    });
  }

  ngOnDestroy() {
    this.ngbModalRef = null;
  }
}
