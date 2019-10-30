import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { filter, map } from 'rxjs/operators';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';

import { ISelectorToTemplate } from 'app/shared/model/selector-to-template.model';
import { AccountService } from 'app/core/auth/account.service';
import { SelectorToTemplateService } from './selector-to-template.service';

@Component({
  selector: 'jhi-selector-to-template',
  templateUrl: './selector-to-template.component.html'
})
export class SelectorToTemplateComponent implements OnInit, OnDestroy {
  selectorToTemplates: ISelectorToTemplate[];
  currentAccount: any;
  eventSubscriber: Subscription;

  constructor(
    protected selectorToTemplateService: SelectorToTemplateService,
    protected jhiAlertService: JhiAlertService,
    protected eventManager: JhiEventManager,
    protected accountService: AccountService
  ) {}

  loadAll() {
    this.selectorToTemplateService
      .query()
      .pipe(
        filter((res: HttpResponse<ISelectorToTemplate[]>) => res.ok),
        map((res: HttpResponse<ISelectorToTemplate[]>) => res.body)
      )
      .subscribe(
        (res: ISelectorToTemplate[]) => {
          this.selectorToTemplates = res;
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  ngOnInit() {
    this.loadAll();
    this.accountService.identity().then(account => {
      this.currentAccount = account;
    });
    this.registerChangeInSelectorToTemplates();
  }

  ngOnDestroy() {
    this.eventManager.destroy(this.eventSubscriber);
  }

  trackId(index: number, item: ISelectorToTemplate) {
    return item.id;
  }

  registerChangeInSelectorToTemplates() {
    this.eventSubscriber = this.eventManager.subscribe('selectorToTemplateListModification', response => this.loadAll());
  }

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }
}
