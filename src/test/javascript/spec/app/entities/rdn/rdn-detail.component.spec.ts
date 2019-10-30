import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { RDNDetailComponent } from 'app/entities/rdn/rdn-detail.component';
import { RDN } from 'app/shared/model/rdn.model';

describe('Component Tests', () => {
  describe('RDN Management Detail Component', () => {
    let comp: RDNDetailComponent;
    let fixture: ComponentFixture<RDNDetailComponent>;
    const route = ({ data: of({ rDN: new RDN(123) }) } as any) as ActivatedRoute;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [RDNDetailComponent],
        providers: [{ provide: ActivatedRoute, useValue: route }]
      })
        .overrideTemplate(RDNDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(RDNDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should call load all on init', () => {
        // GIVEN

        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.rDN).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
