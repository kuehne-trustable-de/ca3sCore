import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { ICertificate } from 'app/shared/model/certificate.model';
import { CertificateService } from './certificate.service';

@Component({
  selector: 'jhi-certificate-delete-dialog',
  templateUrl: './certificate-delete-dialog.component.html'
})
export class CertificateDeleteDialogComponent {
  certificate: ICertificate;

  constructor(
    protected certificateService: CertificateService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  clear() {
    this.activeModal.dismiss('cancel');
  }

  confirmDelete(id: number) {
    this.certificateService.delete(id).subscribe(response => {
      this.eventManager.broadcast({
        name: 'certificateListModification',
        content: 'Deleted an certificate'
      });
      this.activeModal.dismiss(true);
    });
  }
}

@Component({
  selector: 'jhi-certificate-delete-popup',
  template: ''
})
export class CertificateDeletePopupComponent implements OnInit, OnDestroy {
  protected ngbModalRef: NgbModalRef;

  constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ certificate }) => {
      setTimeout(() => {
        this.ngbModalRef = this.modalService.open(CertificateDeleteDialogComponent as Component, { size: 'lg', backdrop: 'static' });
        this.ngbModalRef.componentInstance.certificate = certificate;
        this.ngbModalRef.result.then(
          result => {
            this.router.navigate(['/certificate', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          },
          reason => {
            this.router.navigate(['/certificate', { outlets: { popup: null } }]);
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
