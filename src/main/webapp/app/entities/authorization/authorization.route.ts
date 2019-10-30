import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes } from '@angular/router';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { Observable, of } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { Authorization } from 'app/shared/model/authorization.model';
import { AuthorizationService } from './authorization.service';
import { AuthorizationComponent } from './authorization.component';
import { AuthorizationDetailComponent } from './authorization-detail.component';
import { AuthorizationUpdateComponent } from './authorization-update.component';
import { AuthorizationDeletePopupComponent } from './authorization-delete-dialog.component';
import { IAuthorization } from 'app/shared/model/authorization.model';

@Injectable({ providedIn: 'root' })
export class AuthorizationResolve implements Resolve<IAuthorization> {
  constructor(private service: AuthorizationService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<IAuthorization> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        filter((response: HttpResponse<Authorization>) => response.ok),
        map((authorization: HttpResponse<Authorization>) => authorization.body)
      );
    }
    return of(new Authorization());
  }
}

export const authorizationRoute: Routes = [
  {
    path: '',
    component: AuthorizationComponent,
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.authorization.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/view',
    component: AuthorizationDetailComponent,
    resolve: {
      authorization: AuthorizationResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.authorization.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'new',
    component: AuthorizationUpdateComponent,
    resolve: {
      authorization: AuthorizationResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.authorization.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/edit',
    component: AuthorizationUpdateComponent,
    resolve: {
      authorization: AuthorizationResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.authorization.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const authorizationPopupRoute: Routes = [
  {
    path: ':id/delete',
    component: AuthorizationDeletePopupComponent,
    resolve: {
      authorization: AuthorizationResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.authorization.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
