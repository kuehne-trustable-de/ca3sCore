import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { RequestAttributeUpdateComponent } from 'app/entities/request-attribute/request-attribute-update.component';
import { RequestAttributeService } from 'app/entities/request-attribute/request-attribute.service';
import { RequestAttribute } from 'app/shared/model/request-attribute.model';

describe('Component Tests', () => {
  describe('RequestAttribute Management Update Component', () => {
    let comp: RequestAttributeUpdateComponent;
    let fixture: ComponentFixture<RequestAttributeUpdateComponent>;
    let service: RequestAttributeService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [RequestAttributeUpdateComponent],
        providers: [FormBuilder]
      })
        .overrideTemplate(RequestAttributeUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(RequestAttributeUpdateComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(RequestAttributeService);
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', fakeAsync(() => {
        // GIVEN
        const entity = new RequestAttribute(123);
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
        const entity = new RequestAttribute();
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
