import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { Ca3SJhSharedModule } from 'app/shared/shared.module';
import { CAConnectorConfigComponent } from './ca-connector-config.component';
import { CAConnectorConfigDetailComponent } from './ca-connector-config-detail.component';
import { CAConnectorConfigUpdateComponent } from './ca-connector-config-update.component';
import {
  CAConnectorConfigDeletePopupComponent,
  CAConnectorConfigDeleteDialogComponent
} from './ca-connector-config-delete-dialog.component';
import { cAConnectorConfigRoute, cAConnectorConfigPopupRoute } from './ca-connector-config.route';

const ENTITY_STATES = [...cAConnectorConfigRoute, ...cAConnectorConfigPopupRoute];

@NgModule({
  imports: [Ca3SJhSharedModule, RouterModule.forChild(ENTITY_STATES)],
  declarations: [
    CAConnectorConfigComponent,
    CAConnectorConfigDetailComponent,
    CAConnectorConfigUpdateComponent,
    CAConnectorConfigDeleteDialogComponent,
    CAConnectorConfigDeletePopupComponent
  ],
  entryComponents: [CAConnectorConfigDeleteDialogComponent]
})
export class Ca3SJhCAConnectorConfigModule {}
