import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { RequestAttributeValueUpdateComponent } from 'app/entities/request-attribute-value/request-attribute-value-update.component';
import { RequestAttributeValueService } from 'app/entities/request-attribute-value/request-attribute-value.service';
import { RequestAttributeValue } from 'app/shared/model/request-attribute-value.model';

describe('Component Tests', () => {
  describe('RequestAttributeValue Management Update Component', () => {
    let comp: RequestAttributeValueUpdateComponent;
    let fixture: ComponentFixture<RequestAttributeValueUpdateComponent>;
    let service: RequestAttributeValueService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [RequestAttributeValueUpdateComponent],
        providers: [FormBuilder]
      })
        .overrideTemplate(RequestAttributeValueUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(RequestAttributeValueUpdateComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(RequestAttributeValueService);
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', fakeAsync(() => {
        // GIVEN
        const entity = new RequestAttributeValue(123);
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
        const entity = new RequestAttributeValue();
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
