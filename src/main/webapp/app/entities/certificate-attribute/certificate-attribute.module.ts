import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { Ca3SJhSharedModule } from 'app/shared/shared.module';
import { CertificateAttributeComponent } from './certificate-attribute.component';
import { CertificateAttributeDetailComponent } from './certificate-attribute-detail.component';
import { CertificateAttributeUpdateComponent } from './certificate-attribute-update.component';
import {
  CertificateAttributeDeletePopupComponent,
  CertificateAttributeDeleteDialogComponent
} from './certificate-attribute-delete-dialog.component';
import { certificateAttributeRoute, certificateAttributePopupRoute } from './certificate-attribute.route';

const ENTITY_STATES = [...certificateAttributeRoute, ...certificateAttributePopupRoute];

@NgModule({
  imports: [Ca3SJhSharedModule, RouterModule.forChild(ENTITY_STATES)],
  declarations: [
    CertificateAttributeComponent,
    CertificateAttributeDetailComponent,
    CertificateAttributeUpdateComponent,
    CertificateAttributeDeleteDialogComponent,
    CertificateAttributeDeletePopupComponent
  ],
  entryComponents: [CertificateAttributeDeleteDialogComponent]
})
export class Ca3SJhCertificateAttributeModule {}
