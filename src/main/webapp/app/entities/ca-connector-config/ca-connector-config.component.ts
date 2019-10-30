import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { filter, map } from 'rxjs/operators';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';

import { ICAConnectorConfig } from 'app/shared/model/ca-connector-config.model';
import { AccountService } from 'app/core/auth/account.service';
import { CAConnectorConfigService } from './ca-connector-config.service';

@Component({
  selector: 'jhi-ca-connector-config',
  templateUrl: './ca-connector-config.component.html'
})
export class CAConnectorConfigComponent implements OnInit, OnDestroy {
  cAConnectorConfigs: ICAConnectorConfig[];
  currentAccount: any;
  eventSubscriber: Subscription;

  constructor(
    protected cAConnectorConfigService: CAConnectorConfigService,
    protected jhiAlertService: JhiAlertService,
    protected eventManager: JhiEventManager,
    protected accountService: AccountService
  ) {}

  loadAll() {
    this.cAConnectorConfigService
      .query()
      .pipe(
        filter((res: HttpResponse<ICAConnectorConfig[]>) => res.ok),
        map((res: HttpResponse<ICAConnectorConfig[]>) => res.body)
      )
      .subscribe(
        (res: ICAConnectorConfig[]) => {
          this.cAConnectorConfigs = res;
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  ngOnInit() {
    this.loadAll();
    this.accountService.identity().then(account => {
      this.currentAccount = account;
    });
    this.registerChangeInCAConnectorConfigs();
  }

  ngOnDestroy() {
    this.eventManager.destroy(this.eventSubscriber);
  }

  trackId(index: number, item: ICAConnectorConfig) {
    return item.id;
  }

  registerChangeInCAConnectorConfigs() {
    this.eventSubscriber = this.eventManager.subscribe('cAConnectorConfigListModification', response => this.loadAll());
  }

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }
}
