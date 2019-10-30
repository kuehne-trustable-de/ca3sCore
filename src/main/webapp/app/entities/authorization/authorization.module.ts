import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { Ca3SJhSharedModule } from 'app/shared/shared.module';
import { AuthorizationComponent } from './authorization.component';
import { AuthorizationDetailComponent } from './authorization-detail.component';
import { AuthorizationUpdateComponent } from './authorization-update.component';
import { AuthorizationDeletePopupComponent, AuthorizationDeleteDialogComponent } from './authorization-delete-dialog.component';
import { authorizationRoute, authorizationPopupRoute } from './authorization.route';

const ENTITY_STATES = [...authorizationRoute, ...authorizationPopupRoute];

@NgModule({
  imports: [Ca3SJhSharedModule, RouterModule.forChild(ENTITY_STATES)],
  declarations: [
    AuthorizationComponent,
    AuthorizationDetailComponent,
    AuthorizationUpdateComponent,
    AuthorizationDeleteDialogComponent,
    AuthorizationDeletePopupComponent
  ],
  entryComponents: [AuthorizationDeleteDialogComponent]
})
export class Ca3SJhAuthorizationModule {}
