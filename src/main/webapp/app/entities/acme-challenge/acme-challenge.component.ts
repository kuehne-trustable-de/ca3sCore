import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { filter, map } from 'rxjs/operators';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';

import { IAcmeChallenge } from 'app/shared/model/acme-challenge.model';
import { AccountService } from 'app/core/auth/account.service';
import { AcmeChallengeService } from './acme-challenge.service';

@Component({
  selector: 'jhi-acme-challenge',
  templateUrl: './acme-challenge.component.html'
})
export class AcmeChallengeComponent implements OnInit, OnDestroy {
  acmeChallenges: IAcmeChallenge[];
  currentAccount: any;
  eventSubscriber: Subscription;

  constructor(
    protected acmeChallengeService: AcmeChallengeService,
    protected jhiAlertService: JhiAlertService,
    protected eventManager: JhiEventManager,
    protected accountService: AccountService
  ) {}

  loadAll() {
    this.acmeChallengeService
      .query()
      .pipe(
        filter((res: HttpResponse<IAcmeChallenge[]>) => res.ok),
        map((res: HttpResponse<IAcmeChallenge[]>) => res.body)
      )
      .subscribe(
        (res: IAcmeChallenge[]) => {
          this.acmeChallenges = res;
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  ngOnInit() {
    this.loadAll();
    this.accountService.identity().then(account => {
      this.currentAccount = account;
    });
    this.registerChangeInAcmeChallenges();
  }

  ngOnDestroy() {
    this.eventManager.destroy(this.eventSubscriber);
  }

  trackId(index: number, item: IAcmeChallenge) {
    return item.id;
  }

  registerChangeInAcmeChallenges() {
    this.eventSubscriber = this.eventManager.subscribe('acmeChallengeListModification', response => this.loadAll());
  }

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }
}
