import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IRDNAttribute } from 'app/shared/model/rdn-attribute.model';
import { RDNAttributeService } from './rdn-attribute.service';

@Component({
  selector: 'jhi-rdn-attribute-delete-dialog',
  templateUrl: './rdn-attribute-delete-dialog.component.html'
})
export class RDNAttributeDeleteDialogComponent {
  rDNAttribute: IRDNAttribute;

  constructor(
    protected rDNAttributeService: RDNAttributeService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  clear() {
    this.activeModal.dismiss('cancel');
  }

  confirmDelete(id: number) {
    this.rDNAttributeService.delete(id).subscribe(response => {
      this.eventManager.broadcast({
        name: 'rDNAttributeListModification',
        content: 'Deleted an rDNAttribute'
      });
      this.activeModal.dismiss(true);
    });
  }
}

@Component({
  selector: 'jhi-rdn-attribute-delete-popup',
  template: ''
})
export class RDNAttributeDeletePopupComponent implements OnInit, OnDestroy {
  protected ngbModalRef: NgbModalRef;

  constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ rDNAttribute }) => {
      setTimeout(() => {
        this.ngbModalRef = this.modalService.open(RDNAttributeDeleteDialogComponent as Component, { size: 'lg', backdrop: 'static' });
        this.ngbModalRef.componentInstance.rDNAttribute = rDNAttribute;
        this.ngbModalRef.result.then(
          result => {
            this.router.navigate(['/rdn-attribute', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          },
          reason => {
            this.router.navigate(['/rdn-attribute', { outlets: { popup: null } }]);
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
