import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes } from '@angular/router';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { Observable, of } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { SelectorToTemplate } from 'app/shared/model/selector-to-template.model';
import { SelectorToTemplateService } from './selector-to-template.service';
import { SelectorToTemplateComponent } from './selector-to-template.component';
import { SelectorToTemplateDetailComponent } from './selector-to-template-detail.component';
import { SelectorToTemplateUpdateComponent } from './selector-to-template-update.component';
import { SelectorToTemplateDeletePopupComponent } from './selector-to-template-delete-dialog.component';
import { ISelectorToTemplate } from 'app/shared/model/selector-to-template.model';

@Injectable({ providedIn: 'root' })
export class SelectorToTemplateResolve implements Resolve<ISelectorToTemplate> {
  constructor(private service: SelectorToTemplateService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<ISelectorToTemplate> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        filter((response: HttpResponse<SelectorToTemplate>) => response.ok),
        map((selectorToTemplate: HttpResponse<SelectorToTemplate>) => selectorToTemplate.body)
      );
    }
    return of(new SelectorToTemplate());
  }
}

export const selectorToTemplateRoute: Routes = [
  {
    path: '',
    component: SelectorToTemplateComponent,
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.selectorToTemplate.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/view',
    component: SelectorToTemplateDetailComponent,
    resolve: {
      selectorToTemplate: SelectorToTemplateResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.selectorToTemplate.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'new',
    component: SelectorToTemplateUpdateComponent,
    resolve: {
      selectorToTemplate: SelectorToTemplateResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.selectorToTemplate.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/edit',
    component: SelectorToTemplateUpdateComponent,
    resolve: {
      selectorToTemplate: SelectorToTemplateResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.selectorToTemplate.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const selectorToTemplatePopupRoute: Routes = [
  {
    path: ':id/delete',
    component: SelectorToTemplateDeletePopupComponent,
    resolve: {
      selectorToTemplate: SelectorToTemplateResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.selectorToTemplate.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
