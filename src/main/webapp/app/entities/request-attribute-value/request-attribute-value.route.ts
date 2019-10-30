import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes } from '@angular/router';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { Observable, of } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { RequestAttributeValue } from 'app/shared/model/request-attribute-value.model';
import { RequestAttributeValueService } from './request-attribute-value.service';
import { RequestAttributeValueComponent } from './request-attribute-value.component';
import { RequestAttributeValueDetailComponent } from './request-attribute-value-detail.component';
import { RequestAttributeValueUpdateComponent } from './request-attribute-value-update.component';
import { RequestAttributeValueDeletePopupComponent } from './request-attribute-value-delete-dialog.component';
import { IRequestAttributeValue } from 'app/shared/model/request-attribute-value.model';

@Injectable({ providedIn: 'root' })
export class RequestAttributeValueResolve implements Resolve<IRequestAttributeValue> {
  constructor(private service: RequestAttributeValueService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<IRequestAttributeValue> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        filter((response: HttpResponse<RequestAttributeValue>) => response.ok),
        map((requestAttributeValue: HttpResponse<RequestAttributeValue>) => requestAttributeValue.body)
      );
    }
    return of(new RequestAttributeValue());
  }
}

export const requestAttributeValueRoute: Routes = [
  {
    path: '',
    component: RequestAttributeValueComponent,
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.requestAttributeValue.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/view',
    component: RequestAttributeValueDetailComponent,
    resolve: {
      requestAttributeValue: RequestAttributeValueResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.requestAttributeValue.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'new',
    component: RequestAttributeValueUpdateComponent,
    resolve: {
      requestAttributeValue: RequestAttributeValueResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.requestAttributeValue.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/edit',
    component: RequestAttributeValueUpdateComponent,
    resolve: {
      requestAttributeValue: RequestAttributeValueResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.requestAttributeValue.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const requestAttributeValuePopupRoute: Routes = [
  {
    path: ':id/delete',
    component: RequestAttributeValueDeletePopupComponent,
    resolve: {
      requestAttributeValue: RequestAttributeValueResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.requestAttributeValue.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
