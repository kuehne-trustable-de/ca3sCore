import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IAcmeContact } from 'app/shared/model/acme-contact.model';
import { AcmeContactService } from './acme-contact.service';

@Component({
  selector: 'jhi-acme-contact-delete-dialog',
  templateUrl: './acme-contact-delete-dialog.component.html'
})
export class AcmeContactDeleteDialogComponent {
  acmeContact: IAcmeContact;

  constructor(
    protected acmeContactService: AcmeContactService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  clear() {
    this.activeModal.dismiss('cancel');
  }

  confirmDelete(id: number) {
    this.acmeContactService.delete(id).subscribe(response => {
      this.eventManager.broadcast({
        name: 'acmeContactListModification',
        content: 'Deleted an acmeContact'
      });
      this.activeModal.dismiss(true);
    });
  }
}

@Component({
  selector: 'jhi-acme-contact-delete-popup',
  template: ''
})
export class AcmeContactDeletePopupComponent implements OnInit, OnDestroy {
  protected ngbModalRef: NgbModalRef;

  constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ acmeContact }) => {
      setTimeout(() => {
        this.ngbModalRef = this.modalService.open(AcmeContactDeleteDialogComponent as Component, { size: 'lg', backdrop: 'static' });
        this.ngbModalRef.componentInstance.acmeContact = acmeContact;
        this.ngbModalRef.result.then(
          result => {
            this.router.navigate(['/acme-contact', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          },
          reason => {
            this.router.navigate(['/acme-contact', { outlets: { popup: null } }]);
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
