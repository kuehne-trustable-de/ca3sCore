import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ICertificateAttribute } from 'app/shared/model/certificate-attribute.model';

@Component({
  selector: 'jhi-certificate-attribute-detail',
  templateUrl: './certificate-attribute-detail.component.html'
})
export class CertificateAttributeDetailComponent implements OnInit {
  certificateAttribute: ICertificateAttribute;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ certificateAttribute }) => {
      this.certificateAttribute = certificateAttribute;
    });
  }

  previousState() {
    window.history.back();
  }
}
