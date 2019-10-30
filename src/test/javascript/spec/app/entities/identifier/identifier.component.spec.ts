import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { HttpHeaders, HttpResponse } from '@angular/common/http';

import { Ca3SJhTestModule } from '../../../test.module';
import { IdentifierComponent } from 'app/entities/identifier/identifier.component';
import { IdentifierService } from 'app/entities/identifier/identifier.service';
import { Identifier } from 'app/shared/model/identifier.model';

describe('Component Tests', () => {
  describe('Identifier Management Component', () => {
    let comp: IdentifierComponent;
    let fixture: ComponentFixture<IdentifierComponent>;
    let service: IdentifierService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [IdentifierComponent],
        providers: []
      })
        .overrideTemplate(IdentifierComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(IdentifierComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(IdentifierService);
    });

    it('Should call load all on init', () => {
      // GIVEN
      const headers = new HttpHeaders().append('link', 'link;link');
      spyOn(service, 'query').and.returnValue(
        of(
          new HttpResponse({
            body: [new Identifier(123)],
            headers
          })
        )
      );

      // WHEN
      comp.ngOnInit();

      // THEN
      expect(service.query).toHaveBeenCalled();
      expect(comp.identifiers[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
  });
});
