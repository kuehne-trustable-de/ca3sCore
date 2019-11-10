import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { NonceUpdateComponent } from 'app/entities/nonce/nonce-update.component';
import { NonceService } from 'app/entities/nonce/nonce.service';
import { Nonce } from 'app/shared/model/nonce.model';

describe('Component Tests', () => {
  describe('Nonce Management Update Component', () => {
    let comp: NonceUpdateComponent;
    let fixture: ComponentFixture<NonceUpdateComponent>;
    let service: NonceService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [NonceUpdateComponent],
        providers: [FormBuilder]
      })
        .overrideTemplate(NonceUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(NonceUpdateComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(NonceService);
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', fakeAsync(() => {
        // GIVEN
        const entity = new Nonce(123);
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
        const entity = new Nonce();
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
