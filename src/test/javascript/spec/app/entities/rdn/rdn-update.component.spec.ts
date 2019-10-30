import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { RDNUpdateComponent } from 'app/entities/rdn/rdn-update.component';
import { RDNService } from 'app/entities/rdn/rdn.service';
import { RDN } from 'app/shared/model/rdn.model';

describe('Component Tests', () => {
  describe('RDN Management Update Component', () => {
    let comp: RDNUpdateComponent;
    let fixture: ComponentFixture<RDNUpdateComponent>;
    let service: RDNService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [RDNUpdateComponent],
        providers: [FormBuilder]
      })
        .overrideTemplate(RDNUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(RDNUpdateComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(RDNService);
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', fakeAsync(() => {
        // GIVEN
        const entity = new RDN(123);
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
        const entity = new RDN();
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
