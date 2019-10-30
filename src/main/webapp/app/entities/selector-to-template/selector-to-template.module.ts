import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { Ca3SJhSharedModule } from 'app/shared/shared.module';
import { SelectorToTemplateComponent } from './selector-to-template.component';
import { SelectorToTemplateDetailComponent } from './selector-to-template-detail.component';
import { SelectorToTemplateUpdateComponent } from './selector-to-template-update.component';
import {
  SelectorToTemplateDeletePopupComponent,
  SelectorToTemplateDeleteDialogComponent
} from './selector-to-template-delete-dialog.component';
import { selectorToTemplateRoute, selectorToTemplatePopupRoute } from './selector-to-template.route';

const ENTITY_STATES = [...selectorToTemplateRoute, ...selectorToTemplatePopupRoute];

@NgModule({
  imports: [Ca3SJhSharedModule, RouterModule.forChild(ENTITY_STATES)],
  declarations: [
    SelectorToTemplateComponent,
    SelectorToTemplateDetailComponent,
    SelectorToTemplateUpdateComponent,
    SelectorToTemplateDeleteDialogComponent,
    SelectorToTemplateDeletePopupComponent
  ],
  entryComponents: [SelectorToTemplateDeleteDialogComponent]
})
export class Ca3SJhSelectorToTemplateModule {}
