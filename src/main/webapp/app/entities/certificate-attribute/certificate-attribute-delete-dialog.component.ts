import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { ICertificateAttribute } from 'app/shared/model/certificate-attribute.model';
import { CertificateAttributeService } from './certificate-attribute.service';

@Component({
  selector: 'jhi-certificate-attribute-delete-dialog',
  templateUrl: './certificate-attribute-delete-dialog.component.html'
})
export class CertificateAttributeDeleteDialogComponent {
  certificateAttribute: ICertificateAttribute;

  constructor(
    protected certificateAttributeService: CertificateAttributeService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  clear() {
    this.activeModal.dismiss('cancel');
  }

  confirmDelete(id: number) {
    this.certificateAttributeService.delete(id).subscribe(response => {
      this.eventManager.broadcast({
        name: 'certificateAttributeListModification',
        content: 'Deleted an certificateAttribute'
      });
      this.activeModal.dismiss(true);
    });
  }
}

@Component({
  selector: 'jhi-certificate-attribute-delete-popup',
  template: ''
})
export class CertificateAttributeDeletePopupComponent implements OnInit, OnDestroy {
  protected ngbModalRef: NgbModalRef;

  constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ certificateAttribute }) => {
      setTimeout(() => {
        this.ngbModalRef = this.modalService.open(CertificateAttributeDeleteDialogComponent as Component, {
          size: 'lg',
          backdrop: 'static'
        });
        this.ngbModalRef.componentInstance.certificateAttribute = certificateAttribute;
        this.ngbModalRef.result.then(
          result => {
            this.router.navigate(['/certificate-attribute', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          },
          reason => {
            this.router.navigate(['/certificate-attribute', { outlets: { popup: null } }]);
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
