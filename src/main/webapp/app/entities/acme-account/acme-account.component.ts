import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { filter, map } from 'rxjs/operators';
import { JhiEventManager, JhiAlertService, JhiDataUtils } from 'ng-jhipster';

import { IACMEAccount } from 'app/shared/model/acme-account.model';
import { AccountService } from 'app/core/auth/account.service';
import { ACMEAccountService } from './acme-account.service';

@Component({
  selector: 'jhi-acme-account',
  templateUrl: './acme-account.component.html'
})
export class ACMEAccountComponent implements OnInit, OnDestroy {
  aCMEAccounts: IACMEAccount[];
  currentAccount: any;
  eventSubscriber: Subscription;

  constructor(
    protected aCMEAccountService: ACMEAccountService,
    protected jhiAlertService: JhiAlertService,
    protected dataUtils: JhiDataUtils,
    protected eventManager: JhiEventManager,
    protected accountService: AccountService
  ) {}

  loadAll() {
    this.aCMEAccountService
      .query()
      .pipe(
        filter((res: HttpResponse<IACMEAccount[]>) => res.ok),
        map((res: HttpResponse<IACMEAccount[]>) => res.body)
      )
      .subscribe(
        (res: IACMEAccount[]) => {
          this.aCMEAccounts = res;
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  ngOnInit() {
    this.loadAll();
    this.accountService.identity().then(account => {
      this.currentAccount = account;
    });
    this.registerChangeInACMEAccounts();
  }

  ngOnDestroy() {
    this.eventManager.destroy(this.eventSubscriber);
  }

  trackId(index: number, item: IACMEAccount) {
    return item.id;
  }

  byteSize(field) {
    return this.dataUtils.byteSize(field);
  }

  openFile(contentType, field) {
    return this.dataUtils.openFile(contentType, field);
  }

  registerChangeInACMEAccounts() {
    this.eventSubscriber = this.eventManager.subscribe('aCMEAccountListModification', response => this.loadAll());
  }

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }
}
