import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { CSRUpdateComponent } from 'app/entities/csr/csr-update.component';
import { CSRService } from 'app/entities/csr/csr.service';
import { CSR } from 'app/shared/model/csr.model';

describe('Component Tests', () => {
  describe('CSR Management Update Component', () => {
    let comp: CSRUpdateComponent;
    let fixture: ComponentFixture<CSRUpdateComponent>;
    let service: CSRService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [CSRUpdateComponent],
        providers: [FormBuilder]
      })
        .overrideTemplate(CSRUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(CSRUpdateComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(CSRService);
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', fakeAsync(() => {
        // GIVEN
        const entity = new CSR(123);
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
        const entity = new CSR();
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
