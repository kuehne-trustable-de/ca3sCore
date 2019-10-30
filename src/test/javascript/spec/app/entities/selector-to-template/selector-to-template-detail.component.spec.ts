import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { SelectorToTemplateDetailComponent } from 'app/entities/selector-to-template/selector-to-template-detail.component';
import { SelectorToTemplate } from 'app/shared/model/selector-to-template.model';

describe('Component Tests', () => {
  describe('SelectorToTemplate Management Detail Component', () => {
    let comp: SelectorToTemplateDetailComponent;
    let fixture: ComponentFixture<SelectorToTemplateDetailComponent>;
    const route = ({ data: of({ selectorToTemplate: new SelectorToTemplate(123) }) } as any) as ActivatedRoute;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [SelectorToTemplateDetailComponent],
        providers: [{ provide: ActivatedRoute, useValue: route }]
      })
        .overrideTemplate(SelectorToTemplateDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(SelectorToTemplateDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should call load all on init', () => {
        // GIVEN

        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.selectorToTemplate).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
