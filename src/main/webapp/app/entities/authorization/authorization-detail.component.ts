import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IAuthorization } from 'app/shared/model/authorization.model';

@Component({
  selector: 'jhi-authorization-detail',
  templateUrl: './authorization-detail.component.html'
})
export class AuthorizationDetailComponent implements OnInit {
  authorization: IAuthorization;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ authorization }) => {
      this.authorization = authorization;
    });
  }

  previousState() {
    window.history.back();
  }
}
