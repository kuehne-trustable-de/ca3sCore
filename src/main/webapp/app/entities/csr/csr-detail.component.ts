import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { JhiDataUtils } from 'ng-jhipster';

import { ICSR } from 'app/shared/model/csr.model';

@Component({
  selector: 'jhi-csr-detail',
  templateUrl: './csr-detail.component.html'
})
export class CSRDetailComponent implements OnInit {
  cSR: ICSR;

  constructor(protected dataUtils: JhiDataUtils, protected activatedRoute: ActivatedRoute) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ cSR }) => {
      this.cSR = cSR;
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
