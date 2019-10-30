import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { HttpHeaders, HttpResponse } from '@angular/common/http';

import { Ca3SJhTestModule } from '../../../test.module';
import { RequestAttributeValueComponent } from 'app/entities/request-attribute-value/request-attribute-value.component';
import { RequestAttributeValueService } from 'app/entities/request-attribute-value/request-attribute-value.service';
import { RequestAttributeValue } from 'app/shared/model/request-attribute-value.model';

describe('Component Tests', () => {
  describe('RequestAttributeValue Management Component', () => {
    let comp: RequestAttributeValueComponent;
    let fixture: ComponentFixture<RequestAttributeValueComponent>;
    let service: RequestAttributeValueService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [RequestAttributeValueComponent],
        providers: []
      })
        .overrideTemplate(RequestAttributeValueComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(RequestAttributeValueComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(RequestAttributeValueService);
    });

    it('Should call load all on init', () => {
      // GIVEN
      const headers = new HttpHeaders().append('link', 'link;link');
      spyOn(service, 'query').and.returnValue(
        of(
          new HttpResponse({
            body: [new RequestAttributeValue(123)],
            headers
          })
        )
      );

      // WHEN
      comp.ngOnInit();

      // THEN
      expect(service.query).toHaveBeenCalled();
      expect(comp.requestAttributeValues[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
  });
});
