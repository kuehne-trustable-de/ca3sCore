import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IRDN } from 'app/shared/model/rdn.model';

@Component({
  selector: 'jhi-rdn-detail',
  templateUrl: './rdn-detail.component.html'
})
export class RDNDetailComponent implements OnInit {
  rDN: IRDN;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ rDN }) => {
      this.rDN = rDN;
    });
  }

  previousState() {
    window.history.back();
  }
}
