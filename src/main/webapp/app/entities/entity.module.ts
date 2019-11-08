import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

@NgModule({
  imports: [
    RouterModule.forChild([
      {
        path: 'certificate',
        loadChildren: () => import('./certificate/certificate.module').then(m => m.Ca3SJhCertificateModule)
      },
      {
        path: 'csr',
        loadChildren: () => import('./csr/csr.module').then(m => m.Ca3SJhCSRModule)
      }
      /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
    ])
  ]
})
export class Ca3SJhEntityModule {}
