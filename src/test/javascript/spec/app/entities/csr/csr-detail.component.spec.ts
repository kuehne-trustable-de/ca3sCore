import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { CSRDetailComponent } from 'app/entities/csr/csr-detail.component';
import { CSR } from 'app/shared/model/csr.model';

describe('Component Tests', () => {
  describe('CSR Management Detail Component', () => {
    let comp: CSRDetailComponent;
    let fixture: ComponentFixture<CSRDetailComponent>;
    const route = ({ data: of({ cSR: new CSR(123) }) } as any) as ActivatedRoute;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [CSRDetailComponent],
        providers: [{ provide: ActivatedRoute, useValue: route }]
      })
        .overrideTemplate(CSRDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(CSRDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should call load all on init', () => {
        // GIVEN

        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.cSR).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
