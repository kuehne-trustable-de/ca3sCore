import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { CsrAttributeDetailComponent } from 'app/entities/csr-attribute/csr-attribute-detail.component';
import { CsrAttribute } from 'app/shared/model/csr-attribute.model';

describe('Component Tests', () => {
  describe('CsrAttribute Management Detail Component', () => {
    let comp: CsrAttributeDetailComponent;
    let fixture: ComponentFixture<CsrAttributeDetailComponent>;
    const route = ({ data: of({ csrAttribute: new CsrAttribute(123) }) } as any) as ActivatedRoute;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [CsrAttributeDetailComponent],
        providers: [{ provide: ActivatedRoute, useValue: route }]
      })
        .overrideTemplate(CsrAttributeDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(CsrAttributeDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should call load all on init', () => {
        // GIVEN

        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.csrAttribute).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
