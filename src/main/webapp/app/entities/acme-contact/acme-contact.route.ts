import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes } from '@angular/router';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { Observable, of } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { AcmeContact } from 'app/shared/model/acme-contact.model';
import { AcmeContactService } from './acme-contact.service';
import { AcmeContactComponent } from './acme-contact.component';
import { AcmeContactDetailComponent } from './acme-contact-detail.component';
import { AcmeContactUpdateComponent } from './acme-contact-update.component';
import { AcmeContactDeletePopupComponent } from './acme-contact-delete-dialog.component';
import { IAcmeContact } from 'app/shared/model/acme-contact.model';

@Injectable({ providedIn: 'root' })
export class AcmeContactResolve implements Resolve<IAcmeContact> {
  constructor(private service: AcmeContactService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<IAcmeContact> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        filter((response: HttpResponse<AcmeContact>) => response.ok),
        map((acmeContact: HttpResponse<AcmeContact>) => acmeContact.body)
      );
    }
    return of(new AcmeContact());
  }
}

export const acmeContactRoute: Routes = [
  {
    path: '',
    component: AcmeContactComponent,
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.acmeContact.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/view',
    component: AcmeContactDetailComponent,
    resolve: {
      acmeContact: AcmeContactResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.acmeContact.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'new',
    component: AcmeContactUpdateComponent,
    resolve: {
      acmeContact: AcmeContactResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.acmeContact.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/edit',
    component: AcmeContactUpdateComponent,
    resolve: {
      acmeContact: AcmeContactResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.acmeContact.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const acmeContactPopupRoute: Routes = [
  {
    path: ':id/delete',
    component: AcmeContactDeletePopupComponent,
    resolve: {
      acmeContact: AcmeContactResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.acmeContact.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
