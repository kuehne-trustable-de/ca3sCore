import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { Ca3SJhTestModule } from '../../../test.module';
import { CAConnectorConfigDetailComponent } from 'app/entities/ca-connector-config/ca-connector-config-detail.component';
import { CAConnectorConfig } from 'app/shared/model/ca-connector-config.model';

describe('Component Tests', () => {
  describe('CAConnectorConfig Management Detail Component', () => {
    let comp: CAConnectorConfigDetailComponent;
    let fixture: ComponentFixture<CAConnectorConfigDetailComponent>;
    const route = ({ data: of({ cAConnectorConfig: new CAConnectorConfig(123) }) } as any) as ActivatedRoute;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [CAConnectorConfigDetailComponent],
        providers: [{ provide: ActivatedRoute, useValue: route }]
      })
        .overrideTemplate(CAConnectorConfigDetailComponent, '')
        .compileComponents();
      fixture = TestBed.createComponent(CAConnectorConfigDetailComponent);
      comp = fixture.componentInstance;
    });

    describe('OnInit', () => {
      it('Should call load all on init', () => {
        // GIVEN

        // WHEN
        comp.ngOnInit();

        // THEN
        expect(comp.cAConnectorConfig).toEqual(jasmine.objectContaining({ id: 123 }));
      });
    });
  });
});
