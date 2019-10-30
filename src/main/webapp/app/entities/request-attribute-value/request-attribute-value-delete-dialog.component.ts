import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IRequestAttributeValue } from 'app/shared/model/request-attribute-value.model';
import { RequestAttributeValueService } from './request-attribute-value.service';

@Component({
  selector: 'jhi-request-attribute-value-delete-dialog',
  templateUrl: './request-attribute-value-delete-dialog.component.html'
})
export class RequestAttributeValueDeleteDialogComponent {
  requestAttributeValue: IRequestAttributeValue;

  constructor(
    protected requestAttributeValueService: RequestAttributeValueService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  clear() {
    this.activeModal.dismiss('cancel');
  }

  confirmDelete(id: number) {
    this.requestAttributeValueService.delete(id).subscribe(response => {
      this.eventManager.broadcast({
        name: 'requestAttributeValueListModification',
        content: 'Deleted an requestAttributeValue'
      });
      this.activeModal.dismiss(true);
    });
  }
}

@Component({
  selector: 'jhi-request-attribute-value-delete-popup',
  template: ''
})
export class RequestAttributeValueDeletePopupComponent implements OnInit, OnDestroy {
  protected ngbModalRef: NgbModalRef;

  constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ requestAttributeValue }) => {
      setTimeout(() => {
        this.ngbModalRef = this.modalService.open(RequestAttributeValueDeleteDialogComponent as Component, {
          size: 'lg',
          backdrop: 'static'
        });
        this.ngbModalRef.componentInstance.requestAttributeValue = requestAttributeValue;
        this.ngbModalRef.result.then(
          result => {
            this.router.navigate(['/request-attribute-value', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          },
          reason => {
            this.router.navigate(['/request-attribute-value', { outlets: { popup: null } }]);
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
