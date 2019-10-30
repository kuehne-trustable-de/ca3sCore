import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { Ca3SJhSharedModule } from 'app/shared/shared.module';
import { RDNAttributeComponent } from './rdn-attribute.component';
import { RDNAttributeDetailComponent } from './rdn-attribute-detail.component';
import { RDNAttributeUpdateComponent } from './rdn-attribute-update.component';
import { RDNAttributeDeletePopupComponent, RDNAttributeDeleteDialogComponent } from './rdn-attribute-delete-dialog.component';
import { rDNAttributeRoute, rDNAttributePopupRoute } from './rdn-attribute.route';

const ENTITY_STATES = [...rDNAttributeRoute, ...rDNAttributePopupRoute];

@NgModule({
  imports: [Ca3SJhSharedModule, RouterModule.forChild(ENTITY_STATES)],
  declarations: [
    RDNAttributeComponent,
    RDNAttributeDetailComponent,
    RDNAttributeUpdateComponent,
    RDNAttributeDeleteDialogComponent,
    RDNAttributeDeletePopupComponent
  ],
  entryComponents: [RDNAttributeDeleteDialogComponent]
})
export class Ca3SJhRDNAttributeModule {}
