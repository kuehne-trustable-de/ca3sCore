import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes } from '@angular/router';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { Observable, of } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { ACMEAccount } from 'app/shared/model/acme-account.model';
import { ACMEAccountService } from './acme-account.service';
import { ACMEAccountComponent } from './acme-account.component';
import { ACMEAccountDetailComponent } from './acme-account-detail.component';
import { ACMEAccountUpdateComponent } from './acme-account-update.component';
import { ACMEAccountDeletePopupComponent } from './acme-account-delete-dialog.component';
import { IACMEAccount } from 'app/shared/model/acme-account.model';

@Injectable({ providedIn: 'root' })
export class ACMEAccountResolve implements Resolve<IACMEAccount> {
  constructor(private service: ACMEAccountService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<IACMEAccount> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        filter((response: HttpResponse<ACMEAccount>) => response.ok),
        map((aCMEAccount: HttpResponse<ACMEAccount>) => aCMEAccount.body)
      );
    }
    return of(new ACMEAccount());
  }
}

export const aCMEAccountRoute: Routes = [
  {
    path: '',
    component: ACMEAccountComponent,
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.aCMEAccount.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/view',
    component: ACMEAccountDetailComponent,
    resolve: {
      aCMEAccount: ACMEAccountResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.aCMEAccount.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'new',
    component: ACMEAccountUpdateComponent,
    resolve: {
      aCMEAccount: ACMEAccountResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.aCMEAccount.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/edit',
    component: ACMEAccountUpdateComponent,
    resolve: {
      aCMEAccount: ACMEAccountResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.aCMEAccount.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const aCMEAccountPopupRoute: Routes = [
  {
    path: ':id/delete',
    component: ACMEAccountDeletePopupComponent,
    resolve: {
      aCMEAccount: ACMEAccountResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.aCMEAccount.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
