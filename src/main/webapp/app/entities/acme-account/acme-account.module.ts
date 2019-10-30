import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { Ca3SJhSharedModule } from 'app/shared/shared.module';
import { ACMEAccountComponent } from './acme-account.component';
import { ACMEAccountDetailComponent } from './acme-account-detail.component';
import { ACMEAccountUpdateComponent } from './acme-account-update.component';
import { ACMEAccountDeletePopupComponent, ACMEAccountDeleteDialogComponent } from './acme-account-delete-dialog.component';
import { aCMEAccountRoute, aCMEAccountPopupRoute } from './acme-account.route';

const ENTITY_STATES = [...aCMEAccountRoute, ...aCMEAccountPopupRoute];

@NgModule({
  imports: [Ca3SJhSharedModule, RouterModule.forChild(ENTITY_STATES)],
  declarations: [
    ACMEAccountComponent,
    ACMEAccountDetailComponent,
    ACMEAccountUpdateComponent,
    ACMEAccountDeleteDialogComponent,
    ACMEAccountDeletePopupComponent
  ],
  entryComponents: [ACMEAccountDeleteDialogComponent]
})
export class Ca3SJhACMEAccountModule {}
