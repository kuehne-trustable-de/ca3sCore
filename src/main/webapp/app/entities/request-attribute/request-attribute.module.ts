import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { Ca3SJhSharedModule } from 'app/shared/shared.module';
import { RequestAttributeComponent } from './request-attribute.component';
import { RequestAttributeDetailComponent } from './request-attribute-detail.component';
import { RequestAttributeUpdateComponent } from './request-attribute-update.component';
import { RequestAttributeDeletePopupComponent, RequestAttributeDeleteDialogComponent } from './request-attribute-delete-dialog.component';
import { requestAttributeRoute, requestAttributePopupRoute } from './request-attribute.route';

const ENTITY_STATES = [...requestAttributeRoute, ...requestAttributePopupRoute];

@NgModule({
  imports: [Ca3SJhSharedModule, RouterModule.forChild(ENTITY_STATES)],
  declarations: [
    RequestAttributeComponent,
    RequestAttributeDetailComponent,
    RequestAttributeUpdateComponent,
    RequestAttributeDeleteDialogComponent,
    RequestAttributeDeletePopupComponent
  ],
  entryComponents: [RequestAttributeDeleteDialogComponent]
})
export class Ca3SJhRequestAttributeModule {}
