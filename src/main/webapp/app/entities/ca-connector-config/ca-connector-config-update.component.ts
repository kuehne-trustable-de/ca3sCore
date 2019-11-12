import { Component, OnInit } from '@angular/core';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { ICAConnectorConfig, CAConnectorConfig } from 'app/shared/model/ca-connector-config.model';
import { CAConnectorConfigService } from './ca-connector-config.service';

@Component({
  selector: 'jhi-ca-connector-config-update',
  templateUrl: './ca-connector-config-update.component.html'
})
export class CAConnectorConfigUpdateComponent implements OnInit {
  isSaving: boolean;

  editForm = this.fb.group({
    id: [],
    configId: [null, [Validators.required]],
    name: [],
    caConnectorType: [],
    caUrl: [],
    secret: [],
    pollingOffset: [],
    defaultCA: [],
    active: []
  });

  constructor(
    protected cAConnectorConfigService: CAConnectorConfigService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ cAConnectorConfig }) => {
      this.updateForm(cAConnectorConfig);
    });
  }

  updateForm(cAConnectorConfig: ICAConnectorConfig) {
    this.editForm.patchValue({
      id: cAConnectorConfig.id,
      configId: cAConnectorConfig.configId,
      name: cAConnectorConfig.name,
      caConnectorType: cAConnectorConfig.caConnectorType,
      caUrl: cAConnectorConfig.caUrl,
      secret: cAConnectorConfig.secret,
      pollingOffset: cAConnectorConfig.pollingOffset,
      defaultCA: cAConnectorConfig.defaultCA,
      active: cAConnectorConfig.active
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const cAConnectorConfig = this.createFromForm();
    if (cAConnectorConfig.id !== undefined) {
      this.subscribeToSaveResponse(this.cAConnectorConfigService.update(cAConnectorConfig));
    } else {
      this.subscribeToSaveResponse(this.cAConnectorConfigService.create(cAConnectorConfig));
    }
  }

  private createFromForm(): ICAConnectorConfig {
    return {
      ...new CAConnectorConfig(),
      id: this.editForm.get(['id']).value,
      configId: this.editForm.get(['configId']).value,
      name: this.editForm.get(['name']).value,
      caConnectorType: this.editForm.get(['caConnectorType']).value,
      caUrl: this.editForm.get(['caUrl']).value,
      secret: this.editForm.get(['secret']).value,
      pollingOffset: this.editForm.get(['pollingOffset']).value,
      defaultCA: this.editForm.get(['defaultCA']).value,
      active: this.editForm.get(['active']).value
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ICAConnectorConfig>>) {
    result.subscribe(() => this.onSaveSuccess(), () => this.onSaveError());
  }

  protected onSaveSuccess() {
    this.isSaving = false;
    this.previousState();
  }

  protected onSaveError() {
    this.isSaving = false;
  }
}
