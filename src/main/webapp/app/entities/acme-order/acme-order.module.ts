import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { Ca3SJhSharedModule } from 'app/shared/shared.module';
import { AcmeOrderComponent } from './acme-order.component';
import { AcmeOrderDetailComponent } from './acme-order-detail.component';
import { AcmeOrderUpdateComponent } from './acme-order-update.component';
import { AcmeOrderDeletePopupComponent, AcmeOrderDeleteDialogComponent } from './acme-order-delete-dialog.component';
import { acmeOrderRoute, acmeOrderPopupRoute } from './acme-order.route';

const ENTITY_STATES = [...acmeOrderRoute, ...acmeOrderPopupRoute];

@NgModule({
  imports: [Ca3SJhSharedModule, RouterModule.forChild(ENTITY_STATES)],
  declarations: [
    AcmeOrderComponent,
    AcmeOrderDetailComponent,
    AcmeOrderUpdateComponent,
    AcmeOrderDeleteDialogComponent,
    AcmeOrderDeletePopupComponent
  ],
  entryComponents: [AcmeOrderDeleteDialogComponent]
})
export class Ca3SJhAcmeOrderModule {}
