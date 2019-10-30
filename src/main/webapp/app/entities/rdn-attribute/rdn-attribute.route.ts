import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes } from '@angular/router';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { Observable, of } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { RDNAttribute } from 'app/shared/model/rdn-attribute.model';
import { RDNAttributeService } from './rdn-attribute.service';
import { RDNAttributeComponent } from './rdn-attribute.component';
import { RDNAttributeDetailComponent } from './rdn-attribute-detail.component';
import { RDNAttributeUpdateComponent } from './rdn-attribute-update.component';
import { RDNAttributeDeletePopupComponent } from './rdn-attribute-delete-dialog.component';
import { IRDNAttribute } from 'app/shared/model/rdn-attribute.model';

@Injectable({ providedIn: 'root' })
export class RDNAttributeResolve implements Resolve<IRDNAttribute> {
  constructor(private service: RDNAttributeService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<IRDNAttribute> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        filter((response: HttpResponse<RDNAttribute>) => response.ok),
        map((rDNAttribute: HttpResponse<RDNAttribute>) => rDNAttribute.body)
      );
    }
    return of(new RDNAttribute());
  }
}

export const rDNAttributeRoute: Routes = [
  {
    path: '',
    component: RDNAttributeComponent,
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.rDNAttribute.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/view',
    component: RDNAttributeDetailComponent,
    resolve: {
      rDNAttribute: RDNAttributeResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.rDNAttribute.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'new',
    component: RDNAttributeUpdateComponent,
    resolve: {
      rDNAttribute: RDNAttributeResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.rDNAttribute.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/edit',
    component: RDNAttributeUpdateComponent,
    resolve: {
      rDNAttribute: RDNAttributeResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.rDNAttribute.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const rDNAttributePopupRoute: Routes = [
  {
    path: ':id/delete',
    component: RDNAttributeDeletePopupComponent,
    resolve: {
      rDNAttribute: RDNAttributeResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.rDNAttribute.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
