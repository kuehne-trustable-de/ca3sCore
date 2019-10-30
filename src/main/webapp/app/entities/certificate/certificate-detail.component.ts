import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { JhiDataUtils } from 'ng-jhipster';

import { ICertificate } from 'app/shared/model/certificate.model';

@Component({
  selector: 'jhi-certificate-detail',
  templateUrl: './certificate-detail.component.html'
})
export class CertificateDetailComponent implements OnInit {
  certificate: ICertificate;

  constructor(protected dataUtils: JhiDataUtils, protected activatedRoute: ActivatedRoute) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ certificate }) => {
      this.certificate = certificate;
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
