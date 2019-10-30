import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { ICsrAttribute } from 'app/shared/model/csr-attribute.model';
import { CsrAttributeService } from './csr-attribute.service';

@Component({
  selector: 'jhi-csr-attribute-delete-dialog',
  templateUrl: './csr-attribute-delete-dialog.component.html'
})
export class CsrAttributeDeleteDialogComponent {
  csrAttribute: ICsrAttribute;

  constructor(
    protected csrAttributeService: CsrAttributeService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  clear() {
    this.activeModal.dismiss('cancel');
  }

  confirmDelete(id: number) {
    this.csrAttributeService.delete(id).subscribe(response => {
      this.eventManager.broadcast({
        name: 'csrAttributeListModification',
        content: 'Deleted an csrAttribute'
      });
      this.activeModal.dismiss(true);
    });
  }
}

@Component({
  selector: 'jhi-csr-attribute-delete-popup',
  template: ''
})
export class CsrAttributeDeletePopupComponent implements OnInit, OnDestroy {
  protected ngbModalRef: NgbModalRef;

  constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ csrAttribute }) => {
      setTimeout(() => {
        this.ngbModalRef = this.modalService.open(CsrAttributeDeleteDialogComponent as Component, { size: 'lg', backdrop: 'static' });
        this.ngbModalRef.componentInstance.csrAttribute = csrAttribute;
        this.ngbModalRef.result.then(
          result => {
            this.router.navigate(['/csr-attribute', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          },
          reason => {
            this.router.navigate(['/csr-attribute', { outlets: { popup: null } }]);
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
