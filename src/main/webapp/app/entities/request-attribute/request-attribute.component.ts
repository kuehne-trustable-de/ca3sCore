import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { filter, map } from 'rxjs/operators';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';

import { IRequestAttribute } from 'app/shared/model/request-attribute.model';
import { AccountService } from 'app/core/auth/account.service';
import { RequestAttributeService } from './request-attribute.service';

@Component({
  selector: 'jhi-request-attribute',
  templateUrl: './request-attribute.component.html'
})
export class RequestAttributeComponent implements OnInit, OnDestroy {
  requestAttributes: IRequestAttribute[];
  currentAccount: any;
  eventSubscriber: Subscription;

  constructor(
    protected requestAttributeService: RequestAttributeService,
    protected jhiAlertService: JhiAlertService,
    protected eventManager: JhiEventManager,
    protected accountService: AccountService
  ) {}

  loadAll() {
    this.requestAttributeService
      .query()
      .pipe(
        filter((res: HttpResponse<IRequestAttribute[]>) => res.ok),
        map((res: HttpResponse<IRequestAttribute[]>) => res.body)
      )
      .subscribe(
        (res: IRequestAttribute[]) => {
          this.requestAttributes = res;
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  ngOnInit() {
    this.loadAll();
    this.accountService.identity().then(account => {
      this.currentAccount = account;
    });
    this.registerChangeInRequestAttributes();
  }

  ngOnDestroy() {
    this.eventManager.destroy(this.eventSubscriber);
  }

  trackId(index: number, item: IRequestAttribute) {
    return item.id;
  }

  registerChangeInRequestAttributes() {
    this.eventSubscriber = this.eventManager.subscribe('requestAttributeListModification', response => this.loadAll());
  }

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }
}
