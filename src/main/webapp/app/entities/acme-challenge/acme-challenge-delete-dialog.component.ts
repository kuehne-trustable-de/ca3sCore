import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IAcmeChallenge } from 'app/shared/model/acme-challenge.model';
import { AcmeChallengeService } from './acme-challenge.service';

@Component({
  selector: 'jhi-acme-challenge-delete-dialog',
  templateUrl: './acme-challenge-delete-dialog.component.html'
})
export class AcmeChallengeDeleteDialogComponent {
  acmeChallenge: IAcmeChallenge;

  constructor(
    protected acmeChallengeService: AcmeChallengeService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  clear() {
    this.activeModal.dismiss('cancel');
  }

  confirmDelete(id: number) {
    this.acmeChallengeService.delete(id).subscribe(response => {
      this.eventManager.broadcast({
        name: 'acmeChallengeListModification',
        content: 'Deleted an acmeChallenge'
      });
      this.activeModal.dismiss(true);
    });
  }
}

@Component({
  selector: 'jhi-acme-challenge-delete-popup',
  template: ''
})
export class AcmeChallengeDeletePopupComponent implements OnInit, OnDestroy {
  protected ngbModalRef: NgbModalRef;

  constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ acmeChallenge }) => {
      setTimeout(() => {
        this.ngbModalRef = this.modalService.open(AcmeChallengeDeleteDialogComponent as Component, { size: 'lg', backdrop: 'static' });
        this.ngbModalRef.componentInstance.acmeChallenge = acmeChallenge;
        this.ngbModalRef.result.then(
          result => {
            this.router.navigate(['/acme-challenge', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          },
          reason => {
            this.router.navigate(['/acme-challenge', { outlets: { popup: null } }]);
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
