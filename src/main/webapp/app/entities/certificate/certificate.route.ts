import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes } from '@angular/router';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { Observable, of } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { Certificate } from 'app/shared/model/certificate.model';
import { CertificateService } from './certificate.service';
import { CertificateComponent } from './certificate.component';
import { CertificateDetailComponent } from './certificate-detail.component';
import { CertificateUpdateComponent } from './certificate-update.component';
import { CertificateDeletePopupComponent } from './certificate-delete-dialog.component';
import { ICertificate } from 'app/shared/model/certificate.model';

@Injectable({ providedIn: 'root' })
export class CertificateResolve implements Resolve<ICertificate> {
  constructor(private service: CertificateService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<ICertificate> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        filter((response: HttpResponse<Certificate>) => response.ok),
        map((certificate: HttpResponse<Certificate>) => certificate.body)
      );
    }
    return of(new Certificate());
  }
}

export const certificateRoute: Routes = [
  {
    path: '',
    component: CertificateComponent,
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.certificate.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/view',
    component: CertificateDetailComponent,
    resolve: {
      certificate: CertificateResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.certificate.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'new',
    component: CertificateUpdateComponent,
    resolve: {
      certificate: CertificateResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.certificate.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/edit',
    component: CertificateUpdateComponent,
    resolve: {
      certificate: CertificateResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.certificate.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const certificatePopupRoute: Routes = [
  {
    path: ':id/delete',
    component: CertificateDeletePopupComponent,
    resolve: {
      certificate: CertificateResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.certificate.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
