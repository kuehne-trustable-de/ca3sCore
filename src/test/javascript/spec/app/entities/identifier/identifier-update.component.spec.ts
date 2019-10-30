import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { IdentifierUpdateComponent } from 'app/entities/identifier/identifier-update.component';
import { IdentifierService } from 'app/entities/identifier/identifier.service';
import { Identifier } from 'app/shared/model/identifier.model';

describe('Component Tests', () => {
  describe('Identifier Management Update Component', () => {
    let comp: IdentifierUpdateComponent;
    let fixture: ComponentFixture<IdentifierUpdateComponent>;
    let service: IdentifierService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [IdentifierUpdateComponent],
        providers: [FormBuilder]
      })
        .overrideTemplate(IdentifierUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(IdentifierUpdateComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(IdentifierService);
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', fakeAsync(() => {
        // GIVEN
        const entity = new Identifier(123);
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
        const entity = new Identifier();
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
