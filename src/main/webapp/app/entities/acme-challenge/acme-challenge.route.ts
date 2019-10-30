import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes } from '@angular/router';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { Observable, of } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { AcmeChallenge } from 'app/shared/model/acme-challenge.model';
import { AcmeChallengeService } from './acme-challenge.service';
import { AcmeChallengeComponent } from './acme-challenge.component';
import { AcmeChallengeDetailComponent } from './acme-challenge-detail.component';
import { AcmeChallengeUpdateComponent } from './acme-challenge-update.component';
import { AcmeChallengeDeletePopupComponent } from './acme-challenge-delete-dialog.component';
import { IAcmeChallenge } from 'app/shared/model/acme-challenge.model';

@Injectable({ providedIn: 'root' })
export class AcmeChallengeResolve implements Resolve<IAcmeChallenge> {
  constructor(private service: AcmeChallengeService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<IAcmeChallenge> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        filter((response: HttpResponse<AcmeChallenge>) => response.ok),
        map((acmeChallenge: HttpResponse<AcmeChallenge>) => acmeChallenge.body)
      );
    }
    return of(new AcmeChallenge());
  }
}

export const acmeChallengeRoute: Routes = [
  {
    path: '',
    component: AcmeChallengeComponent,
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.acmeChallenge.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/view',
    component: AcmeChallengeDetailComponent,
    resolve: {
      acmeChallenge: AcmeChallengeResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.acmeChallenge.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'new',
    component: AcmeChallengeUpdateComponent,
    resolve: {
      acmeChallenge: AcmeChallengeResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.acmeChallenge.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/edit',
    component: AcmeChallengeUpdateComponent,
    resolve: {
      acmeChallenge: AcmeChallengeResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.acmeChallenge.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const acmeChallengePopupRoute: Routes = [
  {
    path: ':id/delete',
    component: AcmeChallengeDeletePopupComponent,
    resolve: {
      acmeChallenge: AcmeChallengeResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.acmeChallenge.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
