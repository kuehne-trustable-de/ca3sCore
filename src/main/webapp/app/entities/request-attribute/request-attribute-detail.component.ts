import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IRequestAttribute } from 'app/shared/model/request-attribute.model';

@Component({
  selector: 'jhi-request-attribute-detail',
  templateUrl: './request-attribute-detail.component.html'
})
export class RequestAttributeDetailComponent implements OnInit {
  requestAttribute: IRequestAttribute;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ requestAttribute }) => {
      this.requestAttribute = requestAttribute;
    });
  }

  previousState() {
    window.history.back();
  }
}
