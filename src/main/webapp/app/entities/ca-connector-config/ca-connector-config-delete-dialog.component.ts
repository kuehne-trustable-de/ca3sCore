import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { ICAConnectorConfig } from 'app/shared/model/ca-connector-config.model';
import { CAConnectorConfigService } from './ca-connector-config.service';

@Component({
  selector: 'jhi-ca-connector-config-delete-dialog',
  templateUrl: './ca-connector-config-delete-dialog.component.html'
})
export class CAConnectorConfigDeleteDialogComponent {
  cAConnectorConfig: ICAConnectorConfig;

  constructor(
    protected cAConnectorConfigService: CAConnectorConfigService,
    public activeModal: NgbActiveModal,
    protected eventManager: JhiEventManager
  ) {}

  clear() {
    this.activeModal.dismiss('cancel');
  }

  confirmDelete(id: number) {
    this.cAConnectorConfigService.delete(id).subscribe(response => {
      this.eventManager.broadcast({
        name: 'cAConnectorConfigListModification',
        content: 'Deleted an cAConnectorConfig'
      });
      this.activeModal.dismiss(true);
    });
  }
}

@Component({
  selector: 'jhi-ca-connector-config-delete-popup',
  template: ''
})
export class CAConnectorConfigDeletePopupComponent implements OnInit, OnDestroy {
  protected ngbModalRef: NgbModalRef;

  constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ cAConnectorConfig }) => {
      setTimeout(() => {
        this.ngbModalRef = this.modalService.open(CAConnectorConfigDeleteDialogComponent as Component, { size: 'lg', backdrop: 'static' });
        this.ngbModalRef.componentInstance.cAConnectorConfig = cAConnectorConfig;
        this.ngbModalRef.result.then(
          result => {
            this.router.navigate(['/ca-connector-config', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          },
          reason => {
            this.router.navigate(['/ca-connector-config', { outlets: { popup: null } }]);
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
