import { Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot, Routes } from '@angular/router';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { Observable, of } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { CAConnectorConfig } from 'app/shared/model/ca-connector-config.model';
import { CAConnectorConfigService } from './ca-connector-config.service';
import { CAConnectorConfigComponent } from './ca-connector-config.component';
import { CAConnectorConfigDetailComponent } from './ca-connector-config-detail.component';
import { CAConnectorConfigUpdateComponent } from './ca-connector-config-update.component';
import { CAConnectorConfigDeletePopupComponent } from './ca-connector-config-delete-dialog.component';
import { ICAConnectorConfig } from 'app/shared/model/ca-connector-config.model';

@Injectable({ providedIn: 'root' })
export class CAConnectorConfigResolve implements Resolve<ICAConnectorConfig> {
  constructor(private service: CAConnectorConfigService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<ICAConnectorConfig> {
    const id = route.params['id'];
    if (id) {
      return this.service.find(id).pipe(
        filter((response: HttpResponse<CAConnectorConfig>) => response.ok),
        map((cAConnectorConfig: HttpResponse<CAConnectorConfig>) => cAConnectorConfig.body)
      );
    }
    return of(new CAConnectorConfig());
  }
}

export const cAConnectorConfigRoute: Routes = [
  {
    path: '',
    component: CAConnectorConfigComponent,
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.cAConnectorConfig.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/view',
    component: CAConnectorConfigDetailComponent,
    resolve: {
      cAConnectorConfig: CAConnectorConfigResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.cAConnectorConfig.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'new',
    component: CAConnectorConfigUpdateComponent,
    resolve: {
      cAConnectorConfig: CAConnectorConfigResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.cAConnectorConfig.home.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':id/edit',
    component: CAConnectorConfigUpdateComponent,
    resolve: {
      cAConnectorConfig: CAConnectorConfigResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.cAConnectorConfig.home.title'
    },
    canActivate: [UserRouteAccessService]
  }
];

export const cAConnectorConfigPopupRoute: Routes = [
  {
    path: ':id/delete',
    component: CAConnectorConfigDeletePopupComponent,
    resolve: {
      cAConnectorConfig: CAConnectorConfigResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'ca3SJhApp.cAConnectorConfig.home.title'
    },
    canActivate: [UserRouteAccessService],
    outlet: 'popup'
  }
];
