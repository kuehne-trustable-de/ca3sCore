import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { RequestAttributeValueDetailComponent } from 'app/entities/request-attribute-value/request-attribute-value-detail.component';
import { RequestAttributeValue } from 'app/shared/model/request-attribute-value.model';

describe('Component Tests', () => {
  describe('RequestAttributeValue Management Detail Component', () => {
    let comp: RequestAttributeValueDetailComponent;
    let fixture: ComponentFixture<RequestAttributeValueDetailComponent>;
    const route = ({ data: of({ requestAttributeValue: new RequestAttributeValue(123) }) } as any) as ActivatedRoute;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [RequestAttributeValueDetailComponent],
        providers: [{ provide: ActivatedRoute, useValue: route }]
      })
        .overrideTemplate(RequestAttributeValueDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(RequestAttributeValueDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should call load all on init', () => {
        // GIVEN

        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.requestAttributeValue).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
