import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { HttpHeaders, HttpResponse } from '@angular/common/http';

import { Ca3SJhTestModule } from '../../../test.module';
import { ACMEAccountComponent } from 'app/entities/acme-account/acme-account.component';
import { ACMEAccountService } from 'app/entities/acme-account/acme-account.service';
import { ACMEAccount } from 'app/shared/model/acme-account.model';

describe('Component Tests', () => {
  describe('ACMEAccount Management Component', () => {
    let comp: ACMEAccountComponent;
    let fixture: ComponentFixture<ACMEAccountComponent>;
    let service: ACMEAccountService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [ACMEAccountComponent],
        providers: []
      })
        .overrideTemplate(ACMEAccountComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(ACMEAccountComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(ACMEAccountService);
    });

    it('Should call load all on init', () => {
      // GIVEN
      const headers = new HttpHeaders().append('link', 'link;link');
      spyOn(service, 'query').and.returnValue(
        of(
          new HttpResponse({
            body: [new ACMEAccount(123)],
            headers
          })
        )
      );

      // WHEN
      comp.ngOnInit();

      // THEN
      expect(service.query).toHaveBeenCalled();
      expect(comp.aCMEAccounts[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
  });
});
