import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { Ca3SJhSharedModule } from 'app/shared/shared.module';
import { AcmeContactComponent } from './acme-contact.component';
import { AcmeContactDetailComponent } from './acme-contact-detail.component';
import { AcmeContactUpdateComponent } from './acme-contact-update.component';
import { AcmeContactDeletePopupComponent, AcmeContactDeleteDialogComponent } from './acme-contact-delete-dialog.component';
import { acmeContactRoute, acmeContactPopupRoute } from './acme-contact.route';

const ENTITY_STATES = [...acmeContactRoute, ...acmeContactPopupRoute];

@NgModule({
  imports: [Ca3SJhSharedModule, RouterModule.forChild(ENTITY_STATES)],
  declarations: [
    AcmeContactComponent,
    AcmeContactDetailComponent,
    AcmeContactUpdateComponent,
    AcmeContactDeleteDialogComponent,
    AcmeContactDeletePopupComponent
  ],
  entryComponents: [AcmeContactDeleteDialogComponent]
})
export class Ca3SJhAcmeContactModule {}
