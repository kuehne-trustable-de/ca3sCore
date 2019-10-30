import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { HttpHeaders, HttpResponse } from '@angular/common/http';

import { Ca3SJhTestModule } from '../../../test.module';
import { AuthorizationComponent } from 'app/entities/authorization/authorization.component';
import { AuthorizationService } from 'app/entities/authorization/authorization.service';
import { Authorization } from 'app/shared/model/authorization.model';

describe('Component Tests', () => {
  describe('Authorization Management Component', () => {
    let comp: AuthorizationComponent;
    let fixture: ComponentFixture<AuthorizationComponent>;
    let service: AuthorizationService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [AuthorizationComponent],
        providers: []
      })
        .overrideTemplate(AuthorizationComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(AuthorizationComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(AuthorizationService);
    });

    it('Should call load all on init', () => {
      // GIVEN
      const headers = new HttpHeaders().append('link', 'link;link');
      spyOn(service, 'query').and.returnValue(
        of(
          new HttpResponse({
            body: [new Authorization(123)],
            headers
          })
        )
      );

      // WHEN
      comp.ngOnInit();

      // THEN
      expect(service.query).toHaveBeenCalled();
      expect(comp.authorizations[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
  });
});
