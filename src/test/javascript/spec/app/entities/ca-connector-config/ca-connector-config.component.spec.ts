import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { HttpHeaders, HttpResponse } from '@angular/common/http';

import { Ca3SJhTestModule } from '../../../test.module';
import { CAConnectorConfigComponent } from 'app/entities/ca-connector-config/ca-connector-config.component';
import { CAConnectorConfigService } from 'app/entities/ca-connector-config/ca-connector-config.service';
import { CAConnectorConfig } from 'app/shared/model/ca-connector-config.model';

describe('Component Tests', () => {
  describe('CAConnectorConfig Management Component', () => {
    let comp: CAConnectorConfigComponent;
    let fixture: ComponentFixture<CAConnectorConfigComponent>;
    let service: CAConnectorConfigService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [Ca3SJhTestModule],
        declarations: [CAConnectorConfigComponent],
        providers: []
      })
        .overrideTemplate(CAConnectorConfigComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(CAConnectorConfigComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(CAConnectorConfigService);
    });

    it('Should call load all on init', () => {
      // GIVEN
      const headers = new HttpHeaders().append('link', 'link;link');
      spyOn(service, 'query').and.returnValue(
        of(
          new HttpResponse({
            body: [new CAConnectorConfig(123)],
            headers
          })
        )
      );

      // WHEN
      comp.ngOnInit();

      // THEN
      expect(service.query).toHaveBeenCalled();
      expect(comp.cAConnectorConfigs[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
  });
});
