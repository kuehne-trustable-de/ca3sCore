import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { HttpHeaders, HttpResponse } from '@angular/common/http';

import { Ca3SJhTestModule } from '../../../test.module';
import { NonceComponent } from 'app/entities/nonce/nonce.component';
import { NonceService } from 'app/entities/nonce/nonce.service';
import { Nonce } from 'app/shared/model/nonce.model';

describe('Component Tests', () => {
  describe('Nonce Management Component', () => {
    let comp: NonceComponent;
    let fixture: ComponentFixture<NonceComponent>;
    let service: NonceService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [NonceComponent],
        providers: []
      })
        .overrideTemplate(NonceComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(NonceComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(NonceService);
    });

    it('Should call load all on init', () => {
      // GIVEN
      const headers = new HttpHeaders().append('link', 'link;link');
      spyOn(service, 'query').and.returnValue(
        of(
          new HttpResponse({
            body: [new Nonce(123)],
            headers
          })
        )
      );

      // WHEN
      comp.ngOnInit();

      // THEN
      expect(service.query).toHaveBeenCalled();
      expect(comp.nonces[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
  });
});
