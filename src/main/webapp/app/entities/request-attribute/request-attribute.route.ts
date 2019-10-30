import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes } from '@angular/router';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { Observable, of } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { RequestAttribute } from 'app/shared/model/request-attribute.model';
import { RequestAttributeService } from './request-attribute.service';
import { RequestAttributeComponent } from './request-attribute.component';
import { RequestAttributeDetailComponent } from './request-attribute-detail.component';
import { RequestAttributeUpdateComponent } from './request-attribute-update.component';
import { RequestAttributeDeletePopupComponent } from './request-attribute-delete-dialog.component';
import { IRequestAttribute } from 'app/shared/model/request-attribute.model';

@Injectable({ providedIn: 'root' })
export class RequestAttributeResolve implements Resolve<IRequestAttribute> {
  constructor(private service: RequestAttributeService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<IRequestAttribute> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        filter((response: HttpResponse<RequestAttribute>) => response.ok),
        map((requestAttribute: HttpResponse<RequestAttribute>) => requestAttribute.body)
      );
    }
    return of(new RequestAttribute());
  }
}

export const requestAttributeRoute: Routes = [
  {
    path: '',
    component: RequestAttributeComponent,
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.requestAttribute.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/view',
    component: RequestAttributeDetailComponent,
    resolve: {
      requestAttribute: RequestAttributeResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.requestAttribute.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'new',
    component: RequestAttributeUpdateComponent,
    resolve: {
      requestAttribute: RequestAttributeResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.requestAttribute.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/edit',
    component: RequestAttributeUpdateComponent,
    resolve: {
      requestAttribute: RequestAttributeResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.requestAttribute.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const requestAttributePopupRoute: Routes = [
  {
    path: ':id/delete',
    component: RequestAttributeDeletePopupComponent,
    resolve: {
      requestAttribute: RequestAttributeResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.requestAttribute.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
