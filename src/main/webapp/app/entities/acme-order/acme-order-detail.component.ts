import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IAcmeOrder } from 'app/shared/model/acme-order.model';

@Component({
  selector: 'jhi-acme-order-detail',
  templateUrl: './acme-order-detail.component.html'
})
export class AcmeOrderDetailComponent implements OnInit {
  acmeOrder: IAcmeOrder;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ acmeOrder }) => {
      this.acmeOrder = acmeOrder;
    });
  }

  previousState() {
    window.history.back();
  }
}
