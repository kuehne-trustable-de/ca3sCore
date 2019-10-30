import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IACMEAccount } from 'app/shared/model/acme-account.model';
import { ACMEAccountService } from './acme-account.service';

@Component({
  selector: 'jhi-acme-account-delete-dialog',
  templateUrl: './acme-account-delete-dialog.component.html'
})
export class ACMEAccountDeleteDialogComponent {
  aCMEAccount: IACMEAccount;

  constructor(
    protected aCMEAccountService: ACMEAccountService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  clear() {
    this.activeModal.dismiss('cancel');
  }

  confirmDelete(id: number) {
    this.aCMEAccountService.delete(id).subscribe(response => {
      this.eventManager.broadcast({
        name: 'aCMEAccountListModification',
        content: 'Deleted an aCMEAccount'
      });
      this.activeModal.dismiss(true);
    });
  }
}

@Component({
  selector: 'jhi-acme-account-delete-popup',
  template: ''
})
export class ACMEAccountDeletePopupComponent implements OnInit, OnDestroy {
  protected ngbModalRef: NgbModalRef;

  constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ aCMEAccount }) => {
      setTimeout(() => {
        this.ngbModalRef = this.modalService.open(ACMEAccountDeleteDialogComponent as Component, { size: 'lg', backdrop: 'static' });
        this.ngbModalRef.componentInstance.aCMEAccount = aCMEAccount;
        this.ngbModalRef.result.then(
          result => {
            this.router.navigate(['/acme-account', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          },
          reason => {
            this.router.navigate(['/acme-account', { outlets: { popup: null } }]);
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
