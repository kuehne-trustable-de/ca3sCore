import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { Ca3SJhSharedModule } from 'app/shared/shared.module';
import { CsrAttributeComponent } from './csr-attribute.component';
import { CsrAttributeDetailComponent } from './csr-attribute-detail.component';
import { CsrAttributeUpdateComponent } from './csr-attribute-update.component';
import { CsrAttributeDeletePopupComponent, CsrAttributeDeleteDialogComponent } from './csr-attribute-delete-dialog.component';
import { csrAttributeRoute, csrAttributePopupRoute } from './csr-attribute.route';

const ENTITY_STATES = [...csrAttributeRoute, ...csrAttributePopupRoute];

@NgModule({
  imports: [Ca3SJhSharedModule, RouterModule.forChild(ENTITY_STATES)],
  declarations: [
    CsrAttributeComponent,
    CsrAttributeDetailComponent,
    CsrAttributeUpdateComponent,
    CsrAttributeDeleteDialogComponent,
    CsrAttributeDeletePopupComponent
  ],
  entryComponents: [CsrAttributeDeleteDialogComponent]
})
export class Ca3SJhCsrAttributeModule {}
