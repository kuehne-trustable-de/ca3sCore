import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

@NgModule({
  imports: [
    RouterModule.forChild([
      {
        path: 'ca-connector-config',
        loadChildren: () => import('./ca-connector-config/ca-connector-config.module').then(m => m.Ca3SJhCAConnectorConfigModule)
      },
      {
        path: 'certificate-attribute',
        loadChildren: () => import('./certificate-attribute/certificate-attribute.module').then(m => m.Ca3SJhCertificateAttributeModule)
      },
      {
        path: 'certificate',
        loadChildren: () => import('./certificate/certificate.module').then(m => m.Ca3SJhCertificateModule)
      },
      {
        path: 'selector-to-template',
        loadChildren: () => import('./selector-to-template/selector-to-template.module').then(m => m.Ca3SJhSelectorToTemplateModule)
      },
      {
        path: 'csr',
        loadChildren: () => import('./csr/csr.module').then(m => m.Ca3SJhCSRModule)
      },
      {
        path: 'csr-attribute',
        loadChildren: () => import('./csr-attribute/csr-attribute.module').then(m => m.Ca3SJhCsrAttributeModule)
      },
      {
        path: 'request-attribute',
        loadChildren: () => import('./request-attribute/request-attribute.module').then(m => m.Ca3SJhRequestAttributeModule)
      },
      {
        path: 'request-attribute-value',
        loadChildren: () =>
          import('./request-attribute-value/request-attribute-value.module').then(m => m.Ca3SJhRequestAttributeValueModule)
      },
      {
        path: 'rdn',
        loadChildren: () => import('./rdn/rdn.module').then(m => m.Ca3SJhRDNModule)
      },
      {
        path: 'acme-account',
        loadChildren: () => import('./acme-account/acme-account.module').then(m => m.Ca3SJhACMEAccountModule)
      },
      {
        path: 'acme-order',
        loadChildren: () => import('./acme-order/acme-order.module').then(m => m.Ca3SJhAcmeOrderModule)
      },
      {
        path: 'identifier',
        loadChildren: () => import('./identifier/identifier.module').then(m => m.Ca3SJhIdentifierModule)
      },
      {
        path: 'authorization',
        loadChildren: () => import('./authorization/authorization.module').then(m => m.Ca3SJhAuthorizationModule)
      },
      {
        path: 'acme-contact',
        loadChildren: () => import('./acme-contact/acme-contact.module').then(m => m.Ca3SJhAcmeContactModule)
      },
      {
        path: 'acme-challenge',
        loadChildren: () => import('./acme-challenge/acme-challenge.module').then(m => m.Ca3SJhAcmeChallengeModule)
      },
      {
        path: 'rdn-attribute',
        loadChildren: () => import('./rdn-attribute/rdn-attribute.module').then(m => m.Ca3SJhRDNAttributeModule)
      }
      /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
    ])
  ]
})
export class Ca3SJhEntityModule {}
