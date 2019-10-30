import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { CertificateUpdateComponent } from 'app/entities/certificate/certificate-update.component';
import { CertificateService } from 'app/entities/certificate/certificate.service';
import { Certificate } from 'app/shared/model/certificate.model';

describe('Component Tests', () => {
  describe('Certificate Management Update Component', () => {
    let comp: CertificateUpdateComponent;
    let fixture: ComponentFixture<CertificateUpdateComponent>;
    let service: CertificateService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [CertificateUpdateComponent],
        providers: [FormBuilder]
      })
        .overrideTemplate(CertificateUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(CertificateUpdateComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(CertificateService);
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', fakeAsync(() => {
        // GIVEN
        const entity = new Certificate(123);
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
        const entity = new Certificate();
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
