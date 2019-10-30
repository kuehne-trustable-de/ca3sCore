import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { filter, map } from 'rxjs/operators';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';

import { IAcmeContact } from 'app/shared/model/acme-contact.model';
import { AccountService } from 'app/core/auth/account.service';
import { AcmeContactService } from './acme-contact.service';

@Component({
  selector: 'jhi-acme-contact',
  templateUrl: './acme-contact.component.html'
})
export class AcmeContactComponent implements OnInit, OnDestroy {
  acmeContacts: IAcmeContact[];
  currentAccount: any;
  eventSubscriber: Subscription;

  constructor(
    protected acmeContactService: AcmeContactService,
    protected jhiAlertService: JhiAlertService,
    protected eventManager: JhiEventManager,
    protected accountService: AccountService
  ) {}

  loadAll() {
    this.acmeContactService
      .query()
      .pipe(
        filter((res: HttpResponse<IAcmeContact[]>) => res.ok),
        map((res: HttpResponse<IAcmeContact[]>) => res.body)
      )
      .subscribe(
        (res: IAcmeContact[]) => {
          this.acmeContacts = res;
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  ngOnInit() {
    this.loadAll();
    this.accountService.identity().then(account => {
      this.currentAccount = account;
    });
    this.registerChangeInAcmeContacts();
  }

  ngOnDestroy() {
    this.eventManager.destroy(this.eventSubscriber);
  }

  trackId(index: number, item: IAcmeContact) {
    return item.id;
  }

  registerChangeInAcmeContacts() {
    this.eventSubscriber = this.eventManager.subscribe('acmeContactListModification', response => this.loadAll());
  }

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }
}
