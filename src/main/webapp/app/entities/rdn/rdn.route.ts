import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes } from '@angular/router';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { Observable, of } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { RDN } from 'app/shared/model/rdn.model';
import { RDNService } from './rdn.service';
import { RDNComponent } from './rdn.component';
import { RDNDetailComponent } from './rdn-detail.component';
import { RDNUpdateComponent } from './rdn-update.component';
import { RDNDeletePopupComponent } from './rdn-delete-dialog.component';
import { IRDN } from 'app/shared/model/rdn.model';

@Injectable({ providedIn: 'root' })
export class RDNResolve implements Resolve<IRDN> {
  constructor(private service: RDNService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<IRDN> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        filter((response: HttpResponse<RDN>) => response.ok),
        map((rDN: HttpResponse<RDN>) => rDN.body)
      );
    }
    return of(new RDN());
  }
}

export const rDNRoute: Routes = [
  {
    path: '',
    component: RDNComponent,
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.rDN.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/view',
    component: RDNDetailComponent,
    resolve: {
      rDN: RDNResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.rDN.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'new',
    component: RDNUpdateComponent,
    resolve: {
      rDN: RDNResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.rDN.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/edit',
    component: RDNUpdateComponent,
    resolve: {
      rDN: RDNResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.rDN.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const rDNPopupRoute: Routes = [
  {
    path: ':id/delete',
    component: RDNDeletePopupComponent,
    resolve: {
      rDN: RDNResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.rDN.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
