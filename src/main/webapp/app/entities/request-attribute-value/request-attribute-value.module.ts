import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { Ca3SJhSharedModule } from 'app/shared/shared.module';
import { RequestAttributeValueComponent } from './request-attribute-value.component';
import { RequestAttributeValueDetailComponent } from './request-attribute-value-detail.component';
import { RequestAttributeValueUpdateComponent } from './request-attribute-value-update.component';
import {
  RequestAttributeValueDeletePopupComponent,
  RequestAttributeValueDeleteDialogComponent
} from './request-attribute-value-delete-dialog.component';
import { requestAttributeValueRoute, requestAttributeValuePopupRoute } from './request-attribute-value.route';

const ENTITY_STATES = [...requestAttributeValueRoute, ...requestAttributeValuePopupRoute];

@NgModule({
  imports: [Ca3SJhSharedModule, RouterModule.forChild(ENTITY_STATES)],
  declarations: [
    RequestAttributeValueComponent,
    RequestAttributeValueDetailComponent,
    RequestAttributeValueUpdateComponent,
    RequestAttributeValueDeleteDialogComponent,
    RequestAttributeValueDeletePopupComponent
  ],
  entryComponents: [RequestAttributeValueDeleteDialogComponent]
})
export class Ca3SJhRequestAttributeValueModule {}
