import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { CAConnectorConfigUpdateComponent } from 'app/entities/ca-connector-config/ca-connector-config-update.component';
import { CAConnectorConfigService } from 'app/entities/ca-connector-config/ca-connector-config.service';
import { CAConnectorConfig } from 'app/shared/model/ca-connector-config.model';

describe('Component Tests', () => {
  describe('CAConnectorConfig Management Update Component', () => {
    let comp: CAConnectorConfigUpdateComponent;
    let fixture: ComponentFixture<CAConnectorConfigUpdateComponent>;
    let service: CAConnectorConfigService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [CAConnectorConfigUpdateComponent],
        providers: [FormBuilder]
      })
        .overrideTemplate(CAConnectorConfigUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(CAConnectorConfigUpdateComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(CAConnectorConfigService);
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', fakeAsync(() => {
        // GIVEN
        const entity = new CAConnectorConfig(123);
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
        const entity = new CAConnectorConfig();
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
