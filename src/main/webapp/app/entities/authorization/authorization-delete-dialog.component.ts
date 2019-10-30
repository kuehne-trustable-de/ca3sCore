import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IAuthorization } from 'app/shared/model/authorization.model';
import { AuthorizationService } from './authorization.service';

@Component({
  selector: 'jhi-authorization-delete-dialog',
  templateUrl: './authorization-delete-dialog.component.html'
})
export class AuthorizationDeleteDialogComponent {
  authorization: IAuthorization;

  constructor(
    protected authorizationService: AuthorizationService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  clear() {
    this.activeModal.dismiss('cancel');
  }

  confirmDelete(id: number) {
    this.authorizationService.delete(id).subscribe(response => {
      this.eventManager.broadcast({
        name: 'authorizationListModification',
        content: 'Deleted an authorization'
      });
      this.activeModal.dismiss(true);
    });
  }
}

@Component({
  selector: 'jhi-authorization-delete-popup',
  template: ''
})
export class AuthorizationDeletePopupComponent implements OnInit, OnDestroy {
  protected ngbModalRef: NgbModalRef;

  constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ authorization }) => {
      setTimeout(() => {
        this.ngbModalRef = this.modalService.open(AuthorizationDeleteDialogComponent as Component, { size: 'lg', backdrop: 'static' });
        this.ngbModalRef.componentInstance.authorization = authorization;
        this.ngbModalRef.result.then(
          result => {
            this.router.navigate(['/authorization', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          },
          reason => {
            this.router.navigate(['/authorization', { outlets: { popup: null } }]);
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
