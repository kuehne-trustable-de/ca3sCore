import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { AcmeChallengeUpdateComponent } from 'app/entities/acme-challenge/acme-challenge-update.component';
import { AcmeChallengeService } from 'app/entities/acme-challenge/acme-challenge.service';
import { AcmeChallenge } from 'app/shared/model/acme-challenge.model';

describe('Component Tests', () => {
  describe('AcmeChallenge Management Update Component', () => {
    let comp: AcmeChallengeUpdateComponent;
    let fixture: ComponentFixture<AcmeChallengeUpdateComponent>;
    let service: AcmeChallengeService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [AcmeChallengeUpdateComponent],
        providers: [FormBuilder]
      })
        .overrideTemplate(AcmeChallengeUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(AcmeChallengeUpdateComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(AcmeChallengeService);
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', fakeAsync(() => {
        // GIVEN
        const entity = new AcmeChallenge(123);
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
        const entity = new AcmeChallenge();
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
