import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IRequestAttribute } from 'app/shared/model/request-attribute.model';
import { RequestAttributeService } from './request-attribute.service';

@Component({
  selector: 'jhi-request-attribute-delete-dialog',
  templateUrl: './request-attribute-delete-dialog.component.html'
})
export class RequestAttributeDeleteDialogComponent {
  requestAttribute: IRequestAttribute;

  constructor(
    protected requestAttributeService: RequestAttributeService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  clear() {
    this.activeModal.dismiss('cancel');
  }

  confirmDelete(id: number) {
    this.requestAttributeService.delete(id).subscribe(response => {
      this.eventManager.broadcast({
        name: 'requestAttributeListModification',
        content: 'Deleted an requestAttribute'
      });
      this.activeModal.dismiss(true);
    });
  }
}

@Component({
  selector: 'jhi-request-attribute-delete-popup',
  template: ''
})
export class RequestAttributeDeletePopupComponent implements OnInit, OnDestroy {
  protected ngbModalRef: NgbModalRef;

  constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ requestAttribute }) => {
      setTimeout(() => {
        this.ngbModalRef = this.modalService.open(RequestAttributeDeleteDialogComponent as Component, { size: 'lg', backdrop: 'static' });
        this.ngbModalRef.componentInstance.requestAttribute = requestAttribute;
        this.ngbModalRef.result.then(
          result => {
            this.router.navigate(['/request-attribute', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          },
          reason => {
            this.router.navigate(['/request-attribute', { outlets: { popup: null } }]);
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
