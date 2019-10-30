import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { RequestAttributeDetailComponent } from 'app/entities/request-attribute/request-attribute-detail.component';
import { RequestAttribute } from 'app/shared/model/request-attribute.model';

describe('Component Tests', () => {
  describe('RequestAttribute Management Detail Component', () => {
    let comp: RequestAttributeDetailComponent;
    let fixture: ComponentFixture<RequestAttributeDetailComponent>;
    const route = ({ data: of({ requestAttribute: new RequestAttribute(123) }) } as any) as ActivatedRoute;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [RequestAttributeDetailComponent],
        providers: [{ provide: ActivatedRoute, useValue: route }]
      })
        .overrideTemplate(RequestAttributeDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(RequestAttributeDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should call load all on init', () => {
        // GIVEN

        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.requestAttribute).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
