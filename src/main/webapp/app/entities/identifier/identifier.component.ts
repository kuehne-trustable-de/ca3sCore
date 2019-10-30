import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { filter, map } from 'rxjs/operators';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';

import { IIdentifier } from 'app/shared/model/identifier.model';
import { AccountService } from 'app/core/auth/account.service';
import { IdentifierService } from './identifier.service';

@Component({
  selector: 'jhi-identifier',
  templateUrl: './identifier.component.html'
})
export class IdentifierComponent implements OnInit, OnDestroy {
  identifiers: IIdentifier[];
  currentAccount: any;
  eventSubscriber: Subscription;

  constructor(
    protected identifierService: IdentifierService,
    protected jhiAlertService: JhiAlertService,
    protected eventManager: JhiEventManager,
    protected accountService: AccountService
  ) {}

  loadAll() {
    this.identifierService
      .query()
      .pipe(
        filter((res: HttpResponse<IIdentifier[]>) => res.ok),
        map((res: HttpResponse<IIdentifier[]>) => res.body)
      )
      .subscribe(
        (res: IIdentifier[]) => {
          this.identifiers = res;
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  ngOnInit() {
    this.loadAll();
    this.accountService.identity().then(account => {
      this.currentAccount = account;
    });
    this.registerChangeInIdentifiers();
  }

  ngOnDestroy() {
    this.eventManager.destroy(this.eventSubscriber);
  }

  trackId(index: number, item: IIdentifier) {
    return item.id;
  }

  registerChangeInIdentifiers() {
    this.eventSubscriber = this.eventManager.subscribe('identifierListModification', response => this.loadAll());
  }

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }
}
