import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { Ca3SJhSharedModule } from 'app/shared/shared.module';
import { AcmeChallengeComponent } from './acme-challenge.component';
import { AcmeChallengeDetailComponent } from './acme-challenge-detail.component';
import { AcmeChallengeUpdateComponent } from './acme-challenge-update.component';
import { AcmeChallengeDeletePopupComponent, AcmeChallengeDeleteDialogComponent } from './acme-challenge-delete-dialog.component';
import { acmeChallengeRoute, acmeChallengePopupRoute } from './acme-challenge.route';

const ENTITY_STATES = [...acmeChallengeRoute, ...acmeChallengePopupRoute];

@NgModule({
  imports: [Ca3SJhSharedModule, RouterModule.forChild(ENTITY_STATES)],
  declarations: [
    AcmeChallengeComponent,
    AcmeChallengeDetailComponent,
    AcmeChallengeUpdateComponent,
    AcmeChallengeDeleteDialogComponent,
    AcmeChallengeDeletePopupComponent
  ],
  entryComponents: [AcmeChallengeDeleteDialogComponent]
})
export class Ca3SJhAcmeChallengeModule {}
