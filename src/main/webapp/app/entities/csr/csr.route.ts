import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes } from '@angular/router';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { Observable, of } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { CSR } from 'app/shared/model/csr.model';
import { CSRService } from './csr.service';
import { CSRComponent } from './csr.component';
import { CSRDetailComponent } from './csr-detail.component';
import { CSRUpdateComponent } from './csr-update.component';
import { CSRDeletePopupComponent } from './csr-delete-dialog.component';
import { ICSR } from 'app/shared/model/csr.model';

@Injectable({ providedIn: 'root' })
export class CSRResolve implements Resolve<ICSR> {
  constructor(private service: CSRService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<ICSR> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        filter((response: HttpResponse<CSR>) => response.ok),
        map((cSR: HttpResponse<CSR>) => cSR.body)
      );
    }
    return of(new CSR());
  }
}

export const cSRRoute: Routes = [
  {
    path: '',
    component: CSRComponent,
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.cSR.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/view',
    component: CSRDetailComponent,
    resolve: {
      cSR: CSRResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.cSR.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'new',
    component: CSRUpdateComponent,
    resolve: {
      cSR: CSRResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.cSR.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/edit',
    component: CSRUpdateComponent,
    resolve: {
      cSR: CSRResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.cSR.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const cSRPopupRoute: Routes = [
  {
    path: ':id/delete',
    component: CSRDeletePopupComponent,
    resolve: {
      cSR: CSRResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.cSR.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
