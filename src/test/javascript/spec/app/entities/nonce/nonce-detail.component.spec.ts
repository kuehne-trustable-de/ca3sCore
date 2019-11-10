import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { NonceDetailComponent } from 'app/entities/nonce/nonce-detail.component';
import { Nonce } from 'app/shared/model/nonce.model';

describe('Component Tests', () => {
  describe('Nonce Management Detail Component', () => {
    let comp: NonceDetailComponent;
    let fixture: ComponentFixture<NonceDetailComponent>;
    const route = ({ data: of({ nonce: new Nonce(123) }) } as any) as ActivatedRoute;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [NonceDetailComponent],
        providers: [{ provide: ActivatedRoute, useValue: route }]
      })
        .overrideTemplate(NonceDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(NonceDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should call load all on init', () => {
        // GIVEN

        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.nonce).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
