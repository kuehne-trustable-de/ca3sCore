import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IIdentifier } from 'app/shared/model/identifier.model';

@Component({
  selector: 'jhi-identifier-detail',
  templateUrl: './identifier-detail.component.html'
})
export class IdentifierDetailComponent implements OnInit {
  identifier: IIdentifier;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ identifier }) => {
      this.identifier = identifier;
    });
  }

  previousState() {
    window.history.back();
  }
}
