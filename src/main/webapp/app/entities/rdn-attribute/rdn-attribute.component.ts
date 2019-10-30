import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { filter, map } from 'rxjs/operators';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';

import { IRDNAttribute } from 'app/shared/model/rdn-attribute.model';
import { AccountService } from 'app/core/auth/account.service';
import { RDNAttributeService } from './rdn-attribute.service';

@Component({
  selector: 'jhi-rdn-attribute',
  templateUrl: './rdn-attribute.component.html'
})
export class RDNAttributeComponent implements OnInit, OnDestroy {
  rDNAttributes: IRDNAttribute[];
  currentAccount: any;
  eventSubscriber: Subscription;

  constructor(
    protected rDNAttributeService: RDNAttributeService,
    protected jhiAlertService: JhiAlertService,
    protected eventManager: JhiEventManager,
    protected accountService: AccountService
  ) {}

  loadAll() {
    this.rDNAttributeService
      .query()
      .pipe(
        filter((res: HttpResponse<IRDNAttribute[]>) => res.ok),
        map((res: HttpResponse<IRDNAttribute[]>) => res.body)
      )
      .subscribe(
        (res: IRDNAttribute[]) => {
          this.rDNAttributes = res;
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  ngOnInit() {
    this.loadAll();
    this.accountService.identity().then(account => {
      this.currentAccount = account;
    });
    this.registerChangeInRDNAttributes();
  }

  ngOnDestroy() {
    this.eventManager.destroy(this.eventSubscriber);
  }

  trackId(index: number, item: IRDNAttribute) {
    return item.id;
  }

  registerChangeInRDNAttributes() {
    this.eventSubscriber = this.eventManager.subscribe('rDNAttributeListModification', response => this.loadAll());
  }

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }
}
