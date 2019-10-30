import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { CsrAttributeUpdateComponent } from 'app/entities/csr-attribute/csr-attribute-update.component';
import { CsrAttributeService } from 'app/entities/csr-attribute/csr-attribute.service';
import { CsrAttribute } from 'app/shared/model/csr-attribute.model';

describe('Component Tests', () => {
  describe('CsrAttribute Management Update Component', () => {
    let comp: CsrAttributeUpdateComponent;
    let fixture: ComponentFixture<CsrAttributeUpdateComponent>;
    let service: CsrAttributeService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [CsrAttributeUpdateComponent],
        providers: [FormBuilder]
      })
        .overrideTemplate(CsrAttributeUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(CsrAttributeUpdateComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(CsrAttributeService);
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', fakeAsync(() => {
        // GIVEN
        const entity = new CsrAttribute(123);
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
        const entity = new CsrAttribute();
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
