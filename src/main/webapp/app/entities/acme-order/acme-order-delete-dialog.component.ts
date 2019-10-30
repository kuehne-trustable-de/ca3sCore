import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IAcmeOrder } from 'app/shared/model/acme-order.model';
import { AcmeOrderService } from './acme-order.service';

@Component({
  selector: 'jhi-acme-order-delete-dialog',
  templateUrl: './acme-order-delete-dialog.component.html'
})
export class AcmeOrderDeleteDialogComponent {
  acmeOrder: IAcmeOrder;

  constructor(protected acmeOrderService: AcmeOrderService, public activeModal: NgbActiveModal, protected eventManager: JhiEventManager) {}

  clear() {
    this.activeModal.dismiss('cancel');
  }

  confirmDelete(id: number) {
    this.acmeOrderService.delete(id).subscribe(response => {
      this.eventManager.broadcast({
        name: 'acmeOrderListModification',
        content: 'Deleted an acmeOrder'
      });
      this.activeModal.dismiss(true);
    });
  }
}

@Component({
  selector: 'jhi-acme-order-delete-popup',
  template: ''
})
export class AcmeOrderDeletePopupComponent implements OnInit, OnDestroy {
  protected ngbModalRef: NgbModalRef;

  constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ acmeOrder }) => {
      setTimeout(() => {
        this.ngbModalRef = this.modalService.open(AcmeOrderDeleteDialogComponent as Component, { size: 'lg', backdrop: 'static' });
        this.ngbModalRef.componentInstance.acmeOrder = acmeOrder;
        this.ngbModalRef.result.then(
          result => {
            this.router.navigate(['/acme-order', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          },
          reason => {
            this.router.navigate(['/acme-order', { outlets: { popup: null } }]);
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
