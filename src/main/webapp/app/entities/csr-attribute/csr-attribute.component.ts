import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { filter, map } from 'rxjs/operators';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';

import { ICsrAttribute } from 'app/shared/model/csr-attribute.model';
import { AccountService } from 'app/core/auth/account.service';
import { CsrAttributeService } from './csr-attribute.service';

@Component({
  selector: 'jhi-csr-attribute',
  templateUrl: './csr-attribute.component.html'
})
export class CsrAttributeComponent implements OnInit, OnDestroy {
  csrAttributes: ICsrAttribute[];
  currentAccount: any;
  eventSubscriber: Subscription;

  constructor(
    protected csrAttributeService: CsrAttributeService,
    protected jhiAlertService: JhiAlertService,
    protected eventManager: JhiEventManager,
    protected accountService: AccountService
  ) {}

  loadAll() {
    this.csrAttributeService
      .query()
      .pipe(
        filter((res: HttpResponse<ICsrAttribute[]>) => res.ok),
        map((res: HttpResponse<ICsrAttribute[]>) => res.body)
      )
      .subscribe(
        (res: ICsrAttribute[]) => {
          this.csrAttributes = res;
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  ngOnInit() {
    this.loadAll();
    this.accountService.identity().then(account => {
      this.currentAccount = account;
    });
    this.registerChangeInCsrAttributes();
  }

  ngOnDestroy() {
    this.eventManager.destroy(this.eventSubscriber);
  }

  trackId(index: number, item: ICsrAttribute) {
    return item.id;
  }

  registerChangeInCsrAttributes() {
    this.eventSubscriber = this.eventManager.subscribe('csrAttributeListModification', response => this.loadAll());
  }

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }
}
