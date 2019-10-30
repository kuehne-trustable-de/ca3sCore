import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { Ca3SJhSharedModule } from 'app/shared/shared.module';
import { IdentifierComponent } from './identifier.component';
import { IdentifierDetailComponent } from './identifier-detail.component';
import { IdentifierUpdateComponent } from './identifier-update.component';
import { IdentifierDeletePopupComponent, IdentifierDeleteDialogComponent } from './identifier-delete-dialog.component';
import { identifierRoute, identifierPopupRoute } from './identifier.route';

const ENTITY_STATES = [...identifierRoute, ...identifierPopupRoute];

@NgModule({
  imports: [Ca3SJhSharedModule, RouterModule.forChild(ENTITY_STATES)],
  declarations: [
    IdentifierComponent,
    IdentifierDetailComponent,
    IdentifierUpdateComponent,
    IdentifierDeleteDialogComponent,
    IdentifierDeletePopupComponent
  ],
  entryComponents: [IdentifierDeleteDialogComponent]
})
export class Ca3SJhIdentifierModule {}
