import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IRDN } from 'app/shared/model/rdn.model';
import { RDNService } from './rdn.service';

@Component({
  selector: 'jhi-rdn-delete-dialog',
  templateUrl: './rdn-delete-dialog.component.html'
})
export class RDNDeleteDialogComponent {
  rDN: IRDN;

  constructor(protected rDNService: RDNService, public activeModal: NgbActiveModal, protected eventManager: JhiEventManager) {}

  clear() {
    this.activeModal.dismiss('cancel');
  }

  confirmDelete(id: number) {
    this.rDNService.delete(id).subscribe(response => {
      this.eventManager.broadcast({
        name: 'rDNListModification',
        content: 'Deleted an rDN'
      });
      this.activeModal.dismiss(true);
    });
  }
}

@Component({
  selector: 'jhi-rdn-delete-popup',
  template: ''
})
export class RDNDeletePopupComponent implements OnInit, OnDestroy {
  protected ngbModalRef: NgbModalRef;

  constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ rDN }) => {
      setTimeout(() => {
        this.ngbModalRef = this.modalService.open(RDNDeleteDialogComponent as Component, { size: 'lg', backdrop: 'static' });
        this.ngbModalRef.componentInstance.rDN = rDN;
        this.ngbModalRef.result.then(
          result => {
            this.router.navigate(['/rdn', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          },
          reason => {
            this.router.navigate(['/rdn', { outlets: { popup: null } }]);
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
