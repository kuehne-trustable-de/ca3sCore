import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { RDNAttributeUpdateComponent } from 'app/entities/rdn-attribute/rdn-attribute-update.component';
import { RDNAttributeService } from 'app/entities/rdn-attribute/rdn-attribute.service';
import { RDNAttribute } from 'app/shared/model/rdn-attribute.model';

describe('Component Tests', () => {
  describe('RDNAttribute Management Update Component', () => {
    let comp: RDNAttributeUpdateComponent;
    let fixture: ComponentFixture<RDNAttributeUpdateComponent>;
    let service: RDNAttributeService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [RDNAttributeUpdateComponent],
        providers: [FormBuilder]
      })
        .overrideTemplate(RDNAttributeUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(RDNAttributeUpdateComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(RDNAttributeService);
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', fakeAsync(() => {
        // GIVEN
        const entity = new RDNAttribute(123);
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
        const entity = new RDNAttribute();
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
