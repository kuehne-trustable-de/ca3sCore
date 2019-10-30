import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { AuthorizationUpdateComponent } from 'app/entities/authorization/authorization-update.component';
import { AuthorizationService } from 'app/entities/authorization/authorization.service';
import { Authorization } from 'app/shared/model/authorization.model';

describe('Component Tests', () => {
  describe('Authorization Management Update Component', () => {
    let comp: AuthorizationUpdateComponent;
    let fixture: ComponentFixture<AuthorizationUpdateComponent>;
    let service: AuthorizationService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [AuthorizationUpdateComponent],
        providers: [FormBuilder]
      })
        .overrideTemplate(AuthorizationUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(AuthorizationUpdateComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(AuthorizationService);
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', fakeAsync(() => {
        // GIVEN
        const entity = new Authorization(123);
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
        const entity = new Authorization();
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
