import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { CertificateAttributeDetailComponent } from 'app/entities/certificate-attribute/certificate-attribute-detail.component';
import { CertificateAttribute } from 'app/shared/model/certificate-attribute.model';

describe('Component Tests', () => {
  describe('CertificateAttribute Management Detail Component', () => {
    let comp: CertificateAttributeDetailComponent;
    let fixture: ComponentFixture<CertificateAttributeDetailComponent>;
    const route = ({ data: of({ certificateAttribute: new CertificateAttribute(123) }) } as any) as ActivatedRoute;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [CertificateAttributeDetailComponent],
        providers: [{ provide: ActivatedRoute, useValue: route }]
      })
        .overrideTemplate(CertificateAttributeDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(CertificateAttributeDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should call load all on init', () => {
        // GIVEN

        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.certificateAttribute).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
