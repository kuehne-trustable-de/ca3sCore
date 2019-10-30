import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes } from '@angular/router';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { Observable, of } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { Identifier } from 'app/shared/model/identifier.model';
import { IdentifierService } from './identifier.service';
import { IdentifierComponent } from './identifier.component';
import { IdentifierDetailComponent } from './identifier-detail.component';
import { IdentifierUpdateComponent } from './identifier-update.component';
import { IdentifierDeletePopupComponent } from './identifier-delete-dialog.component';
import { IIdentifier } from 'app/shared/model/identifier.model';

@Injectable({ providedIn: 'root' })
export class IdentifierResolve implements Resolve<IIdentifier> {
  constructor(private service: IdentifierService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<IIdentifier> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        filter((response: HttpResponse<Identifier>) => response.ok),
        map((identifier: HttpResponse<Identifier>) => identifier.body)
      );
    }
    return of(new Identifier());
  }
}

export const identifierRoute: Routes = [
  {
    path: '',
    component: IdentifierComponent,
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.identifier.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/view',
    component: IdentifierDetailComponent,
    resolve: {
      identifier: IdentifierResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.identifier.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'new',
    component: IdentifierUpdateComponent,
    resolve: {
      identifier: IdentifierResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.identifier.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/edit',
    component: IdentifierUpdateComponent,
    resolve: {
      identifier: IdentifierResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.identifier.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const identifierPopupRoute: Routes = [
  {
    path: ':id/delete',
    component: IdentifierDeletePopupComponent,
    resolve: {
      identifier: IdentifierResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.identifier.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
