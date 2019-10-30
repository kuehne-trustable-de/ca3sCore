import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { AcmeContactUpdateComponent } from 'app/entities/acme-contact/acme-contact-update.component';
import { AcmeContactService } from 'app/entities/acme-contact/acme-contact.service';
import { AcmeContact } from 'app/shared/model/acme-contact.model';

describe('Component Tests', () => {
  describe('AcmeContact Management Update Component', () => {
    let comp: AcmeContactUpdateComponent;
    let fixture: ComponentFixture<AcmeContactUpdateComponent>;
    let service: AcmeContactService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [AcmeContactUpdateComponent],
        providers: [FormBuilder]
      })
        .overrideTemplate(AcmeContactUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(AcmeContactUpdateComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(AcmeContactService);
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', fakeAsync(() => {
        // GIVEN
        const entity = new AcmeContact(123);
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
        const entity = new AcmeContact();
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
