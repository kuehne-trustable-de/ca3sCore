import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IRequestAttributeValue } from 'app/shared/model/request-attribute-value.model';

@Component({
  selector: 'jhi-request-attribute-value-detail',
  templateUrl: './request-attribute-value-detail.component.html'
})
export class RequestAttributeValueDetailComponent implements OnInit {
  requestAttributeValue: IRequestAttributeValue;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ requestAttributeValue }) => {
      this.requestAttributeValue = requestAttributeValue;
    });
  }

  previousState() {
    window.history.back();
  }
}
