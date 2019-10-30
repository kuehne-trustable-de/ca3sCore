import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { CertificateDetailComponent } from 'app/entities/certificate/certificate-detail.component';
import { Certificate } from 'app/shared/model/certificate.model';

describe('Component Tests', () => {
  describe('Certificate Management Detail Component', () => {
    let comp: CertificateDetailComponent;
    let fixture: ComponentFixture<CertificateDetailComponent>;
    const route = ({ data: of({ certificate: new Certificate(123) }) } as any) as ActivatedRoute;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [CertificateDetailComponent],
        providers: [{ provide: ActivatedRoute, useValue: route }]
      })
        .overrideTemplate(CertificateDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(CertificateDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should call load all on init', () => {
        // GIVEN

        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.certificate).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
