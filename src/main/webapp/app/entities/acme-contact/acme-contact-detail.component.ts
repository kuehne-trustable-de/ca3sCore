import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IAcmeContact } from 'app/shared/model/acme-contact.model';

@Component({
  selector: 'jhi-acme-contact-detail',
  templateUrl: './acme-contact-detail.component.html'
})
export class AcmeContactDetailComponent implements OnInit {
  acmeContact: IAcmeContact;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ acmeContact }) => {
      this.acmeContact = acmeContact;
    });
  }

  previousState() {
    window.history.back();
  }
}
