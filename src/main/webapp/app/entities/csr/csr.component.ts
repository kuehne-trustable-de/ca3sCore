import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { filter, map } from 'rxjs/operators';
import { JhiEventManager, JhiAlertService, JhiDataUtils } from 'ng-jhipster';

import { ICSR } from 'app/shared/model/csr.model';
import { AccountService } from 'app/core/auth/account.service';
import { CSRService } from './csr.service';

@Component({
  selector: 'jhi-csr',
  templateUrl: './csr.component.html'
})
export class CSRComponent implements OnInit, OnDestroy {
  cSRS: ICSR[];
  currentAccount: any;
  eventSubscriber: Subscription;

  constructor(
    protected cSRService: CSRService,
    protected jhiAlertService: JhiAlertService,
    protected dataUtils: JhiDataUtils,
    protected eventManager: JhiEventManager,
    protected accountService: AccountService
  ) {}

  loadAll() {
    this.cSRService
      .query()
      .pipe(
        filter((res: HttpResponse<ICSR[]>) => res.ok),
        map((res: HttpResponse<ICSR[]>) => res.body)
      )
      .subscribe(
        (res: ICSR[]) => {
          this.cSRS = res;
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  ngOnInit() {
    this.loadAll();
    this.accountService.identity().then(account => {
      this.currentAccount = account;
    });
    this.registerChangeInCSRS();
  }

  ngOnDestroy() {
    this.eventManager.destroy(this.eventSubscriber);
  }

  trackId(index: number, item: ICSR) {
    return item.id;
  }

  byteSize(field) {
    return this.dataUtils.byteSize(field);
  }

  openFile(contentType, field) {
    return this.dataUtils.openFile(contentType, field);
  }

  registerChangeInCSRS() {
    this.eventSubscriber = this.eventManager.subscribe('cSRListModification', response => this.loadAll());
  }

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }
}
