import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { INonce } from 'app/shared/model/nonce.model';
import { NonceService } from './nonce.service';

@Component({
  selector: 'jhi-nonce-delete-dialog',
  templateUrl: './nonce-delete-dialog.component.html'
})
export class NonceDeleteDialogComponent {
  nonce: INonce;

  constructor(protected nonceService: NonceService, public activeModal: NgbActiveModal, protected eventManager: JhiEventManager) {}

  clear() {
    this.activeModal.dismiss('cancel');
  }

  confirmDelete(id: number) {
    this.nonceService.delete(id).subscribe(response => {
      this.eventManager.broadcast({
        name: 'nonceListModification',
        content: 'Deleted an nonce'
      });
      this.activeModal.dismiss(true);
    });
  }
}

@Component({
  selector: 'jhi-nonce-delete-popup',
  template: ''
})
export class NonceDeletePopupComponent implements OnInit, OnDestroy {
  protected ngbModalRef: NgbModalRef;

  constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ nonce }) => {
      setTimeout(() => {
        this.ngbModalRef = this.modalService.open(NonceDeleteDialogComponent as Component, { size: 'lg', backdrop: 'static' });
        this.ngbModalRef.componentInstance.nonce = nonce;
        this.ngbModalRef.result.then(
          result => {
            this.router.navigate(['/nonce', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          },
          reason => {
            this.router.navigate(['/nonce', { outlets: { popup: null } }]);
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
