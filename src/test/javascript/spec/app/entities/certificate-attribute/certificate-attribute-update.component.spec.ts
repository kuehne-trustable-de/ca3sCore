import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { CertificateAttributeUpdateComponent } from 'app/entities/certificate-attribute/certificate-attribute-update.component';
import { CertificateAttributeService } from 'app/entities/certificate-attribute/certificate-attribute.service';
import { CertificateAttribute } from 'app/shared/model/certificate-attribute.model';

describe('Component Tests', () => {
  describe('CertificateAttribute Management Update Component', () => {
    let comp: CertificateAttributeUpdateComponent;
    let fixture: ComponentFixture<CertificateAttributeUpdateComponent>;
    let service: CertificateAttributeService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [CertificateAttributeUpdateComponent],
        providers: [FormBuilder]
      })
        .overrideTemplate(CertificateAttributeUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(CertificateAttributeUpdateComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(CertificateAttributeService);
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', fakeAsync(() => {
        // GIVEN
        const entity = new CertificateAttribute(123);
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
        const entity = new CertificateAttribute();
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
