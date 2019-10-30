import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { filter, map } from 'rxjs/operators';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';

import { IRequestAttributeValue } from 'app/shared/model/request-attribute-value.model';
import { AccountService } from 'app/core/auth/account.service';
import { RequestAttributeValueService } from './request-attribute-value.service';

@Component({
  selector: 'jhi-request-attribute-value',
  templateUrl: './request-attribute-value.component.html'
})
export class RequestAttributeValueComponent implements OnInit, OnDestroy {
  requestAttributeValues: IRequestAttributeValue[];
  currentAccount: any;
  eventSubscriber: Subscription;

  constructor(
    protected requestAttributeValueService: RequestAttributeValueService,
    protected jhiAlertService: JhiAlertService,
    protected eventManager: JhiEventManager,
    protected accountService: AccountService
  ) {}

  loadAll() {
    this.requestAttributeValueService
      .query()
      .pipe(
        filter((res: HttpResponse<IRequestAttributeValue[]>) => res.ok),
        map((res: HttpResponse<IRequestAttributeValue[]>) => res.body)
      )
      .subscribe(
        (res: IRequestAttributeValue[]) => {
          this.requestAttributeValues = res;
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  ngOnInit() {
    this.loadAll();
    this.accountService.identity().then(account => {
      this.currentAccount = account;
    });
    this.registerChangeInRequestAttributeValues();
  }

  ngOnDestroy() {
    this.eventManager.destroy(this.eventSubscriber);
  }

  trackId(index: number, item: IRequestAttributeValue) {
    return item.id;
  }

  registerChangeInRequestAttributeValues() {
    this.eventSubscriber = this.eventManager.subscribe('requestAttributeValueListModification', response => this.loadAll());
  }

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }
}
