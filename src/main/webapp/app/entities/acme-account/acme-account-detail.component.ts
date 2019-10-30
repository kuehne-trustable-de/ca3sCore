import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { JhiDataUtils } from 'ng-jhipster';

import { IACMEAccount } from 'app/shared/model/acme-account.model';

@Component({
  selector: 'jhi-acme-account-detail',
  templateUrl: './acme-account-detail.component.html'
})
export class ACMEAccountDetailComponent implements OnInit {
  aCMEAccount: IACMEAccount;

  constructor(protected dataUtils: JhiDataUtils, protected activatedRoute: ActivatedRoute) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ aCMEAccount }) => {
      this.aCMEAccount = aCMEAccount;
    });
  }

  byteSize(field) {
    return this.dataUtils.byteSize(field);
  }

  openFile(contentType, field) {
    return this.dataUtils.openFile(contentType, field);
  }
  previousState() {
    window.history.back();
  }
}
