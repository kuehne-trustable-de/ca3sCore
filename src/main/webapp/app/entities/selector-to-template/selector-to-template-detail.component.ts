import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ISelectorToTemplate } from 'app/shared/model/selector-to-template.model';

@Component({
  selector: 'jhi-selector-to-template-detail',
  templateUrl: './selector-to-template-detail.component.html'
})
export class SelectorToTemplateDetailComponent implements OnInit {
  selectorToTemplate: ISelectorToTemplate;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ selectorToTemplate }) => {
      this.selectorToTemplate = selectorToTemplate;
    });
  }

  previousState() {
    window.history.back();
  }
}
