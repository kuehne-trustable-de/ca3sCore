import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IIdentifier } from 'app/shared/model/identifier.model';
import { IdentifierService } from './identifier.service';

@Component({
  selector: 'jhi-identifier-delete-dialog',
  templateUrl: './identifier-delete-dialog.component.html'
})
export class IdentifierDeleteDialogComponent {
  identifier: IIdentifier;

  constructor(
    protected identifierService: IdentifierService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  clear() {
    this.activeModal.dismiss('cancel');
  }

  confirmDelete(id: number) {
    this.identifierService.delete(id).subscribe(response => {
      this.eventManager.broadcast({
        name: 'identifierListModification',
        content: 'Deleted an identifier'
      });
      this.activeModal.dismiss(true);
    });
  }
}

@Component({
  selector: 'jhi-identifier-delete-popup',
  template: ''
})
export class IdentifierDeletePopupComponent implements OnInit, OnDestroy {
  protected ngbModalRef: NgbModalRef;

  constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ identifier }) => {
      setTimeout(() => {
        this.ngbModalRef = this.modalService.open(IdentifierDeleteDialogComponent as Component, { size: 'lg', backdrop: 'static' });
        this.ngbModalRef.componentInstance.identifier = identifier;
        this.ngbModalRef.result.then(
          result => {
            this.router.navigate(['/identifier', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          },
          reason => {
            this.router.navigate(['/identifier', { outlets: { popup: null } }]);
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
