import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { Ca3SJhSharedModule } from 'app/shared/shared.module';
import { CertificateComponent } from './certificate.component';
import { CertificateDetailComponent } from './certificate-detail.component';
import { CertificateUpdateComponent } from './certificate-update.component';
import { CertificateDeletePopupComponent, CertificateDeleteDialogComponent } from './certificate-delete-dialog.component';
import { certificateRoute, certificatePopupRoute } from './certificate.route';

const ENTITY_STATES = [...certificateRoute, ...certificatePopupRoute];

@NgModule({
  imports: [Ca3SJhSharedModule, RouterModule.forChild(ENTITY_STATES)],
  declarations: [
    CertificateComponent,
    CertificateDetailComponent,
    CertificateUpdateComponent,
    CertificateDeleteDialogComponent,
    CertificateDeletePopupComponent
  ],
  entryComponents: [CertificateDeleteDialogComponent]
})
export class Ca3SJhCertificateModule {}
