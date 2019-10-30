import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes } from '@angular/router';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { Observable, of } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { CertificateAttribute } from 'app/shared/model/certificate-attribute.model';
import { CertificateAttributeService } from './certificate-attribute.service';
import { CertificateAttributeComponent } from './certificate-attribute.component';
import { CertificateAttributeDetailComponent } from './certificate-attribute-detail.component';
import { CertificateAttributeUpdateComponent } from './certificate-attribute-update.component';
import { CertificateAttributeDeletePopupComponent } from './certificate-attribute-delete-dialog.component';
import { ICertificateAttribute } from 'app/shared/model/certificate-attribute.model';

@Injectable({ providedIn: 'root' })
export class CertificateAttributeResolve implements Resolve<ICertificateAttribute> {
  constructor(private service: CertificateAttributeService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<ICertificateAttribute> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        filter((response: HttpResponse<CertificateAttribute>) => response.ok),
        map((certificateAttribute: HttpResponse<CertificateAttribute>) => certificateAttribute.body)
      );
    }
    return of(new CertificateAttribute());
  }
}

export const certificateAttributeRoute: Routes = [
  {
    path: '',
    component: CertificateAttributeComponent,
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.certificateAttribute.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/view',
    component: CertificateAttributeDetailComponent,
    resolve: {
      certificateAttribute: CertificateAttributeResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.certificateAttribute.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'new',
    component: CertificateAttributeUpdateComponent,
    resolve: {
      certificateAttribute: CertificateAttributeResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.certificateAttribute.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/edit',
    component: CertificateAttributeUpdateComponent,
    resolve: {
      certificateAttribute: CertificateAttributeResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.certificateAttribute.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const certificateAttributePopupRoute: Routes = [
  {
    path: ':id/delete',
    component: CertificateAttributeDeletePopupComponent,
    resolve: {
      certificateAttribute: CertificateAttributeResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.certificateAttribute.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
