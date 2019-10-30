import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { SelectorToTemplateUpdateComponent } from 'app/entities/selector-to-template/selector-to-template-update.component';
import { SelectorToTemplateService } from 'app/entities/selector-to-template/selector-to-template.service';
import { SelectorToTemplate } from 'app/shared/model/selector-to-template.model';

describe('Component Tests', () => {
  describe('SelectorToTemplate Management Update Component', () => {
    let comp: SelectorToTemplateUpdateComponent;
    let fixture: ComponentFixture<SelectorToTemplateUpdateComponent>;
    let service: SelectorToTemplateService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [SelectorToTemplateUpdateComponent],
        providers: [FormBuilder]
      })
        .overrideTemplate(SelectorToTemplateUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(SelectorToTemplateUpdateComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(SelectorToTemplateService);
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', fakeAsync(() => {
        // GIVEN
        const entity = new SelectorToTemplate(123);
        spyOn(service, 'update').and.returnValue(of(new HttpResponse({ body: entity })));
        comp.updateForm(entity);
        // WHEN
        comp.save();
        tick(); // simulate async

        // THEN
        expect(service.update).toHaveBeenCalledWith(entity);
        expect(comp.isSaving).toEqual(false);
      }));

      it('Should call create service on save for new entity', fakeAsync(() => {
        // GIVEN
        const entity = new SelectorToTemplate();
        spyOn(service, 'create').and.returnValue(of(new HttpResponse({ body: entity })));
        comp.updateForm(entity);
        // WHEN
        comp.save();
        tick(); // simulate async

        // THEN
        expect(service.create).toHaveBeenCalledWith(entity);
        expect(comp.isSaving).toEqual(false);
      }));
    });
  });
});
