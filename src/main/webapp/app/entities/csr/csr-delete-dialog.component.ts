import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { ICSR } from 'app/shared/model/csr.model';
import { CSRService } from './csr.service';

@Component({
  selector: 'jhi-csr-delete-dialog',
  templateUrl: './csr-delete-dialog.component.html'
})
export class CSRDeleteDialogComponent {
  cSR: ICSR;

  constructor(protected cSRService: CSRService, public activeModal: NgbActiveModal, protected eventManager: JhiEventManager) {}

  clear() {
    this.activeModal.dismiss('cancel');
  }

  confirmDelete(id: number) {
    this.cSRService.delete(id).subscribe(response => {
      this.eventManager.broadcast({
        name: 'cSRListModification',
        content: 'Deleted an cSR'
      });
      this.activeModal.dismiss(true);
    });
  }
}

@Component({
  selector: 'jhi-csr-delete-popup',
  template: ''
})
export class CSRDeletePopupComponent implements OnInit, OnDestroy {
  protected ngbModalRef: NgbModalRef;

  constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ cSR }) => {
      setTimeout(() => {
        this.ngbModalRef = this.modalService.open(CSRDeleteDialogComponent as Component, { size: 'lg', backdrop: 'static' });
        this.ngbModalRef.componentInstance.cSR = cSR;
        this.ngbModalRef.result.then(
          result => {
            this.router.navigate(['/csr', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          },
          reason => {
            this.router.navigate(['/csr', { outlets: { popup: null } }]);
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
