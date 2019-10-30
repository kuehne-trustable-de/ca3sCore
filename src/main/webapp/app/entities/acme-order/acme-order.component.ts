import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { filter, map } from 'rxjs/operators';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';

import { IAcmeOrder } from 'app/shared/model/acme-order.model';
import { AccountService } from 'app/core/auth/account.service';
import { AcmeOrderService } from './acme-order.service';

@Component({
  selector: 'jhi-acme-order',
  templateUrl: './acme-order.component.html'
})
export class AcmeOrderComponent implements OnInit, OnDestroy {
  acmeOrders: IAcmeOrder[];
  currentAccount: any;
  eventSubscriber: Subscription;

  constructor(
    protected acmeOrderService: AcmeOrderService,
    protected jhiAlertService: JhiAlertService,
    protected eventManager: JhiEventManager,
    protected accountService: AccountService
  ) {}

  loadAll() {
    this.acmeOrderService
      .query()
      .pipe(
        filter((res: HttpResponse<IAcmeOrder[]>) => res.ok),
        map((res: HttpResponse<IAcmeOrder[]>) => res.body)
      )
      .subscribe(
        (res: IAcmeOrder[]) => {
          this.acmeOrders = res;
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  ngOnInit() {
    this.loadAll();
    this.accountService.identity().then(account => {
      this.currentAccount = account;
    });
    this.registerChangeInAcmeOrders();
  }

  ngOnDestroy() {
    this.eventManager.destroy(this.eventSubscriber);
  }

  trackId(index: number, item: IAcmeOrder) {
    return item.id;
  }

  registerChangeInAcmeOrders() {
    this.eventSubscriber = this.eventManager.subscribe('acmeOrderListModification', response => this.loadAll());
  }

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }
}
