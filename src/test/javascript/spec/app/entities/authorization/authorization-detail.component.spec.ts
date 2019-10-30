import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { AuthorizationDetailComponent } from 'app/entities/authorization/authorization-detail.component';
import { Authorization } from 'app/shared/model/authorization.model';

describe('Component Tests', () => {
  describe('Authorization Management Detail Component', () => {
    let comp: AuthorizationDetailComponent;
    let fixture: ComponentFixture<AuthorizationDetailComponent>;
    const route = ({ data: of({ authorization: new Authorization(123) }) } as any) as ActivatedRoute;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [AuthorizationDetailComponent],
        providers: [{ provide: ActivatedRoute, useValue: route }]
      })
        .overrideTemplate(AuthorizationDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(AuthorizationDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should call load all on init', () => {
        // GIVEN

        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.authorization).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
