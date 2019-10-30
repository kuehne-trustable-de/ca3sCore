import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IAcmeChallenge } from 'app/shared/model/acme-challenge.model';

@Component({
  selector: 'jhi-acme-challenge-detail',
  templateUrl: './acme-challenge-detail.component.html'
})
export class AcmeChallengeDetailComponent implements OnInit {
  acmeChallenge: IAcmeChallenge;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ acmeChallenge }) => {
      this.acmeChallenge = acmeChallenge;
    });
  }

  previousState() {
    window.history.back();
  }
}
