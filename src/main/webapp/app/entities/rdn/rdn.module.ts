import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { Ca3SJhSharedModule } from 'app/shared/shared.module';
import { RDNComponent } from './rdn.component';
import { RDNDetailComponent } from './rdn-detail.component';
import { RDNUpdateComponent } from './rdn-update.component';
import { RDNDeletePopupComponent, RDNDeleteDialogComponent } from './rdn-delete-dialog.component';
import { rDNRoute, rDNPopupRoute } from './rdn.route';

const ENTITY_STATES = [...rDNRoute, ...rDNPopupRoute];

@NgModule({
  imports: [Ca3SJhSharedModule, RouterModule.forChild(ENTITY_STATES)],
  declarations: [RDNComponent, RDNDetailComponent, RDNUpdateComponent, RDNDeleteDialogComponent, RDNDeletePopupComponent],
  entryComponents: [RDNDeleteDialogComponent]
})
export class Ca3SJhRDNModule {}
