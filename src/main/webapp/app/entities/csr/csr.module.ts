import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { Ca3SJhSharedModule } from 'app/shared/shared.module';
import { CSRComponent } from './csr.component';
import { CSRDetailComponent } from './csr-detail.component';
import { CSRUpdateComponent } from './csr-update.component';
import { CSRDeletePopupComponent, CSRDeleteDialogComponent } from './csr-delete-dialog.component';
import { cSRRoute, cSRPopupRoute } from './csr.route';

const ENTITY_STATES = [...cSRRoute, ...cSRPopupRoute];

@NgModule({
  imports: [Ca3SJhSharedModule, RouterModule.forChild(ENTITY_STATES)],
  declarations: [CSRComponent, CSRDetailComponent, CSRUpdateComponent, CSRDeleteDialogComponent, CSRDeletePopupComponent],
  entryComponents: [CSRDeleteDialogComponent]
})
export class Ca3SJhCSRModule {}
