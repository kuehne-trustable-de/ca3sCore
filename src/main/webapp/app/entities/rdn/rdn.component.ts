import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { filter, map } from 'rxjs/operators';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';

import { IRDN } from 'app/shared/model/rdn.model';
import { AccountService } from 'app/core/auth/account.service';
import { RDNService } from './rdn.service';

@Component({
  selector: 'jhi-rdn',
  templateUrl: './rdn.component.html'
})
export class RDNComponent implements OnInit, OnDestroy {
  rDNS: IRDN[];
  currentAccount: any;
  eventSubscriber: Subscription;

  constructor(
    protected rDNService: RDNService,
    protected jhiAlertService: JhiAlertService,
    protected eventManager: JhiEventManager,
    protected accountService: AccountService
  ) {}

  loadAll() {
    this.rDNService
      .query()
      .pipe(
        filter((res: HttpResponse<IRDN[]>) => res.ok),
        map((res: HttpResponse<IRDN[]>) => res.body)
      )
      .subscribe(
        (res: IRDN[]) => {
          this.rDNS = res;
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  ngOnInit() {
    this.loadAll();
    this.accountService.identity().then(account => {
      this.currentAccount = account;
    });
    this.registerChangeInRDNS();
  }

  ngOnDestroy() {
    this.eventManager.destroy(this.eventSubscriber);
  }

  trackId(index: number, item: IRDN) {
    return item.id;
  }

  registerChangeInRDNS() {
    this.eventSubscriber = this.eventManager.subscribe('rDNListModification', response => this.loadAll());
  }

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }
}
