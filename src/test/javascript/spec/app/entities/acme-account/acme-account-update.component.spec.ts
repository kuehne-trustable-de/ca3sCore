import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { ACMEAccountUpdateComponent } from 'app/entities/acme-account/acme-account-update.component';
import { ACMEAccountService } from 'app/entities/acme-account/acme-account.service';
import { ACMEAccount } from 'app/shared/model/acme-account.model';

describe('Component Tests', () => {
  describe('ACMEAccount Management Update Component', () => {
    let comp: ACMEAccountUpdateComponent;
    let fixture: ComponentFixture<ACMEAccountUpdateComponent>;
    let service: ACMEAccountService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [ACMEAccountUpdateComponent],
        providers: [FormBuilder]
      })
        .overrideTemplate(ACMEAccountUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(ACMEAccountUpdateComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(ACMEAccountService);
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', fakeAsync(() => {
        // GIVEN
        const entity = new ACMEAccount(123);
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
        const entity = new ACMEAccount();
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
