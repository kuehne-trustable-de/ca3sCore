import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ICAConnectorConfig } from 'app/shared/model/ca-connector-config.model';

@Component({
  selector: 'jhi-ca-connector-config-detail',
  templateUrl: './ca-connector-config-detail.component.html'
})
export class CAConnectorConfigDetailComponent implements OnInit {
  cAConnectorConfig: ICAConnectorConfig;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ cAConnectorConfig }) => {
      this.cAConnectorConfig = cAConnectorConfig;
    });
  }

  previousState() {
    window.history.back();
  }
}
