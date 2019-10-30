import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { HttpHeaders, HttpResponse } from '@angular/common/http';

import { Ca3SJhTestModule } from '../../../test.module';
import { RequestAttributeComponent } from 'app/entities/request-attribute/request-attribute.component';
import { RequestAttributeService } from 'app/entities/request-attribute/request-attribute.service';
import { RequestAttribute } from 'app/shared/model/request-attribute.model';

describe('Component Tests', () => {
  describe('RequestAttribute Management Component', () => {
    let comp: RequestAttributeComponent;
    let fixture: ComponentFixture<RequestAttributeComponent>;
    let service: RequestAttributeService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [RequestAttributeComponent],
        providers: []
      })
        .overrideTemplate(RequestAttributeComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(RequestAttributeComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(RequestAttributeService);
    });

    it('Should call load all on init', () => {
      // GIVEN
      const headers = new HttpHeaders().append('link', 'link;link');
      spyOn(service, 'query').and.returnValue(
        of(
          new HttpResponse({
            body: [new RequestAttribute(123)],
            headers
          })
        )
      );

      // WHEN
      comp.ngOnInit();

      // THEN
      expect(service.query).toHaveBeenCalled();
      expect(comp.requestAttributes[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
  });
});
