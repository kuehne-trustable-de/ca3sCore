import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { Ca3SJhSharedModule } from 'app/shared/shared.module';
import { HOME_ROUTE } from './home.route';
import { HomeComponent } from './home.component';

@NgModule({
  imports: [Ca3SJhSharedModule, RouterModule.forChild([HOME_ROUTE])],
  declarations: [HomeComponent]
})
export class Ca3SJhHomeModule {}
