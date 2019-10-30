import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes } from '@angular/router';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { Observable, of } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { AcmeOrder } from 'app/shared/model/acme-order.model';
import { AcmeOrderService } from './acme-order.service';
import { AcmeOrderComponent } from './acme-order.component';
import { AcmeOrderDetailComponent } from './acme-order-detail.component';
import { AcmeOrderUpdateComponent } from './acme-order-update.component';
import { AcmeOrderDeletePopupComponent } from './acme-order-delete-dialog.component';
import { IAcmeOrder } from 'app/shared/model/acme-order.model';

@Injectable({ providedIn: 'root' })
export class AcmeOrderResolve implements Resolve<IAcmeOrder> {
  constructor(private service: AcmeOrderService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<IAcmeOrder> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        filter((response: HttpResponse<AcmeOrder>) => response.ok),
        map((acmeOrder: HttpResponse<AcmeOrder>) => acmeOrder.body)
      );
    }
    return of(new AcmeOrder());
  }
}

export const acmeOrderRoute: Routes = [
  {
    path: '',
    component: AcmeOrderComponent,
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.acmeOrder.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/view',
    component: AcmeOrderDetailComponent,
    resolve: {
      acmeOrder: AcmeOrderResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.acmeOrder.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'new',
    component: AcmeOrderUpdateComponent,
    resolve: {
      acmeOrder: AcmeOrderResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.acmeOrder.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/edit',
    component: AcmeOrderUpdateComponent,
    resolve: {
      acmeOrder: AcmeOrderResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.acmeOrder.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const acmeOrderPopupRoute: Routes = [
  {
    path: ':id/delete',
    component: AcmeOrderDeletePopupComponent,
    resolve: {
      acmeOrder: AcmeOrderResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.acmeOrder.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
