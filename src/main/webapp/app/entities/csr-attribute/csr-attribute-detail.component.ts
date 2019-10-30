import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ICsrAttribute } from 'app/shared/model/csr-attribute.model';

@Component({
  selector: 'jhi-csr-attribute-detail',
  templateUrl: './csr-attribute-detail.component.html'
})
export class CsrAttributeDetailComponent implements OnInit {
  csrAttribute: ICsrAttribute;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ csrAttribute }) => {
      this.csrAttribute = csrAttribute;
    });
  }

  previousState() {
    window.history.back();
  }
}
