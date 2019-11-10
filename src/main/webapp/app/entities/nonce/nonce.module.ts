import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { Ca3SJhSharedModule } from 'app/shared/shared.module';
import { NonceComponent } from './nonce.component';
import { NonceDetailComponent } from './nonce-detail.component';
import { NonceUpdateComponent } from './nonce-update.component';
import { NonceDeletePopupComponent, NonceDeleteDialogComponent } from './nonce-delete-dialog.component';
import { nonceRoute, noncePopupRoute } from './nonce.route';

const ENTITY_STATES = [...nonceRoute, ...noncePopupRoute];

@NgModule({
  imports: [Ca3SJhSharedModule, RouterModule.forChild(ENTITY_STATES)],
  declarations: [NonceComponent, NonceDetailComponent, NonceUpdateComponent, NonceDeleteDialogComponent, NonceDeletePopupComponent],
  entryComponents: [NonceDeleteDialogComponent]
})
export class Ca3SJhNonceModule {}
