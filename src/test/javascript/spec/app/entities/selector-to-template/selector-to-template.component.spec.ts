import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { HttpHeaders, HttpResponse } from '@angular/common/http';

import { Ca3SJhTestModule } from '../../../test.module';
import { SelectorToTemplateComponent } from 'app/entities/selector-to-template/selector-to-template.component';
import { SelectorToTemplateService } from 'app/entities/selector-to-template/selector-to-template.service';
import { SelectorToTemplate } from 'app/shared/model/selector-to-template.model';

describe('Component Tests', () => {
  describe('SelectorToTemplate Management Component', () => {
    let comp: SelectorToTemplateComponent;
    let fixture: ComponentFixture<SelectorToTemplateComponent>;
    let service: SelectorToTemplateService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [SelectorToTemplateComponent],
        providers: []
      })
        .overrideTemplate(SelectorToTemplateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(SelectorToTemplateComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(SelectorToTemplateService);
    });

    it('Should call load all on init', () => {
      // GIVEN
      const headers = new HttpHeaders().append('link', 'link;link');
      spyOn(service, 'query').and.returnValue(
        of(
          new HttpResponse({
            body: [new SelectorToTemplate(123)],
            headers
          })
        )
      );

      // WHEN
      comp.ngOnInit();

      // THEN
      expect(service.query).toHaveBeenCalled();
      expect(comp.selectorToTemplates[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
  });
});
