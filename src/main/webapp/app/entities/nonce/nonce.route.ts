import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes } from '@angular/router';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { Observable, of } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { Nonce } from 'app/shared/model/nonce.model';
import { NonceService } from './nonce.service';
import { NonceComponent } from './nonce.component';
import { NonceDetailComponent } from './nonce-detail.component';
import { NonceUpdateComponent } from './nonce-update.component';
import { NonceDeletePopupComponent } from './nonce-delete-dialog.component';
import { INonce } from 'app/shared/model/nonce.model';

@Injectable({ providedIn: 'root' })
export class NonceResolve implements Resolve<INonce> {
  constructor(private service: NonceService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<INonce> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        filter((response: HttpResponse<Nonce>) => response.ok),
        map((nonce: HttpResponse<Nonce>) => nonce.body)
      );
    }
    return of(new Nonce());
  }
}

export const nonceRoute: Routes = [
  {
    path: '',
    component: NonceComponent,
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.nonce.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/view',
    component: NonceDetailComponent,
    resolve: {
      nonce: NonceResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.nonce.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'new',
    component: NonceUpdateComponent,
    resolve: {
      nonce: NonceResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.nonce.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/edit',
    component: NonceUpdateComponent,
    resolve: {
      nonce: NonceResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.nonce.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const noncePopupRoute: Routes = [
  {
    path: ':id/delete',
    component: NonceDeletePopupComponent,
    resolve: {
      nonce: NonceResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.nonce.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
