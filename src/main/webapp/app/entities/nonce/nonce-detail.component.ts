import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { INonce } from 'app/shared/model/nonce.model';

@Component({
  selector: 'jhi-nonce-detail',
  templateUrl: './nonce-detail.component.html'
})
export class NonceDetailComponent implements OnInit {
  nonce: INonce;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ nonce }) => {
      this.nonce = nonce;
    });
  }

  previousState() {
    window.history.back();
  }
}
