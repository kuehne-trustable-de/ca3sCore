import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { filter, map } from 'rxjs/operators';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';

import { IAuthorization } from 'app/shared/model/authorization.model';
import { AccountService } from 'app/core/auth/account.service';
import { AuthorizationService } from './authorization.service';

@Component({
  selector: 'jhi-authorization',
  templateUrl: './authorization.component.html'
})
export class AuthorizationComponent implements OnInit, OnDestroy {
  authorizations: IAuthorization[];
  currentAccount: any;
  eventSubscriber: Subscription;

  constructor(
    protected authorizationService: AuthorizationService,
    protected jhiAlertService: JhiAlertService,
    protected eventManager: JhiEventManager,
    protected accountService: AccountService
  ) {}

  loadAll() {
    this.authorizationService
      .query()
      .pipe(
        filter((res: HttpResponse<IAuthorization[]>) => res.ok),
        map((res: HttpResponse<IAuthorization[]>) => res.body)
      )
      .subscribe(
        (res: IAuthorization[]) => {
          this.authorizations = res;
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  ngOnInit() {
    this.loadAll();
    this.accountService.identity().then(account => {
      this.currentAccount = account;
    });
    this.registerChangeInAuthorizations();
  }

  ngOnDestroy() {
    this.eventManager.destroy(this.eventSubscriber);
  }

  trackId(index: number, item: IAuthorization) {
    return item.id;
  }

  registerChangeInAuthorizations() {
    this.eventSubscriber = this.eventManager.subscribe('authorizationListModification', response => this.loadAll());
  }

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }
}
