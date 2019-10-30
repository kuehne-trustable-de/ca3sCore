import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes } from '@angular/router';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { Observable, of } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { CsrAttribute } from 'app/shared/model/csr-attribute.model';
import { CsrAttributeService } from './csr-attribute.service';
import { CsrAttributeComponent } from './csr-attribute.component';
import { CsrAttributeDetailComponent } from './csr-attribute-detail.component';
import { CsrAttributeUpdateComponent } from './csr-attribute-update.component';
import { CsrAttributeDeletePopupComponent } from './csr-attribute-delete-dialog.component';
import { ICsrAttribute } from 'app/shared/model/csr-attribute.model';

@Injectable({ providedIn: 'root' })
export class CsrAttributeResolve implements Resolve<ICsrAttribute> {
  constructor(private service: CsrAttributeService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<ICsrAttribute> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        filter((response: HttpResponse<CsrAttribute>) => response.ok),
        map((csrAttribute: HttpResponse<CsrAttribute>) => csrAttribute.body)
      );
    }
    return of(new CsrAttribute());
  }
}

export const csrAttributeRoute: Routes = [
  {
    path: '',
    component: CsrAttributeComponent,
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.csrAttribute.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/view',
    component: CsrAttributeDetailComponent,
    resolve: {
      csrAttribute: CsrAttributeResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.csrAttribute.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'new',
    component: CsrAttributeUpdateComponent,
    resolve: {
      csrAttribute: CsrAttributeResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.csrAttribute.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/edit',
    component: CsrAttributeUpdateComponent,
    resolve: {
      csrAttribute: CsrAttributeResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.csrAttribute.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const csrAttributePopupRoute: Routes = [
  {
    path: ':id/delete',
    component: CsrAttributeDeletePopupComponent,
    resolve: {
      csrAttribute: CsrAttributeResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.csrAttribute.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
