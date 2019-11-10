import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { filter, map } from 'rxjs/operators';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';

import { INonce } from 'app/shared/model/nonce.model';
import { AccountService } from 'app/core/auth/account.service';
import { NonceService } from './nonce.service';

@Component({
  selector: 'jhi-nonce',
  templateUrl: './nonce.component.html'
})
export class NonceComponent implements OnInit, OnDestroy {
  nonces: INonce[];
  currentAccount: any;
  eventSubscriber: Subscription;

  constructor(
    protected nonceService: NonceService,
    protected jhiAlertService: JhiAlertService,
    protected eventManager: JhiEventManager,
    protected accountService: AccountService
  ) {}

  loadAll() {
    this.nonceService
      .query()
      .pipe(
        filter((res: HttpResponse<INonce[]>) => res.ok),
        map((res: HttpResponse<INonce[]>) => res.body)
      )
      .subscribe(
        (res: INonce[]) => {
          this.nonces = res;
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  ngOnInit() {
    this.loadAll();
    this.accountService.identity().then(account => {
      this.currentAccount = account;
    });
    this.registerChangeInNonces();
  }

  ngOnDestroy() {
    this.eventManager.destroy(this.eventSubscriber);
  }

  trackId(index: number, item: INonce) {
    return item.id;
  }

  registerChangeInNonces() {
    this.eventSubscriber = this.eventManager.subscribe('nonceListModification', response => this.loadAll());
  }

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }
}
