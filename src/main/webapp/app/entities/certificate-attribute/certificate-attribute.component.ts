import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { filter, map } from 'rxjs/operators';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';

import { ICertificateAttribute } from 'app/shared/model/certificate-attribute.model';
import { AccountService } from 'app/core/auth/account.service';
import { CertificateAttributeService } from './certificate-attribute.service';

@Component({
  selector: 'jhi-certificate-attribute',
  templateUrl: './certificate-attribute.component.html'
})
export class CertificateAttributeComponent implements OnInit, OnDestroy {
  certificateAttributes: ICertificateAttribute[];
  currentAccount: any;
  eventSubscriber: Subscription;

  constructor(
    protected certificateAttributeService: CertificateAttributeService,
    protected jhiAlertService: JhiAlertService,
    protected eventManager: JhiEventManager,
    protected accountService: AccountService
  ) {}

  loadAll() {
    this.certificateAttributeService
      .query()
      .pipe(
        filter((res: HttpResponse<ICertificateAttribute[]>) => res.ok),
        map((res: HttpResponse<ICertificateAttribute[]>) => res.body)
      )
      .subscribe(
        (res: ICertificateAttribute[]) => {
          this.certificateAttributes = res;
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  ngOnInit() {
    this.loadAll();
    this.accountService.identity().then(account => {
      this.currentAccount = account;
    });
    this.registerChangeInCertificateAttributes();
  }

  ngOnDestroy() {
    this.eventManager.destroy(this.eventSubscriber);
  }

  trackId(index: number, item: ICertificateAttribute) {
    return item.id;
  }

  registerChangeInCertificateAttributes() {
    this.eventSubscriber = this.eventManager.subscribe('certificateAttributeListModification', response => this.loadAll());
  }

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }
}
