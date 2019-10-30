import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IRDNAttribute } from 'app/shared/model/rdn-attribute.model';

@Component({
  selector: 'jhi-rdn-attribute-detail',
  templateUrl: './rdn-attribute-detail.component.html'
})
export class RDNAttributeDetailComponent implements OnInit {
  rDNAttribute: IRDNAttribute;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ rDNAttribute }) => {
      this.rDNAttribute = rDNAttribute;
    });
  }

  previousState() {
    window.history.back();
  }
}
