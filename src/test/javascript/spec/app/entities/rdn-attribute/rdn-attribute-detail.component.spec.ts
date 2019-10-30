import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { RDNAttributeDetailComponent } from 'app/entities/rdn-attribute/rdn-attribute-detail.component';
import { RDNAttribute } from 'app/shared/model/rdn-attribute.model';

describe('Component Tests', () => {
  describe('RDNAttribute Management Detail Component', () => {
    let comp: RDNAttributeDetailComponent;
    let fixture: ComponentFixture<RDNAttributeDetailComponent>;
    const route = ({ data: of({ rDNAttribute: new RDNAttribute(123) }) } as any) as ActivatedRoute;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [RDNAttributeDetailComponent],
        providers: [{ provide: ActivatedRoute, useValue: route }]
      })
        .overrideTemplate(RDNAttributeDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(RDNAttributeDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should call load all on init', () => {
        // GIVEN

        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.rDNAttribute).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
