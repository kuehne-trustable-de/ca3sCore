import { Component, OnInit } from '@angular/core';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { JhiAlertService } from 'ng-jhipster';
import { IRequestAttribute, RequestAttribute } from 'app/shared/model/request-attribute.model';
import { RequestAttributeService } from './request-attribute.service';
import { IRequestAttributeValue } from 'app/shared/model/request-attribute-value.model';
import { RequestAttributeValueService } from 'app/entities/request-attribute-value/request-attribute-value.service';
import { ICSR } from 'app/shared/model/csr.model';
import { CSRService } from 'app/entities/csr/csr.service';

@Component({
  selector: 'jhi-request-attribute-update',
  templateUrl: './request-attribute-update.component.html'
})
export class RequestAttributeUpdateComponent implements OnInit {
  isSaving: boolean;

  requestattributevalues: IRequestAttributeValue[];

  csrs: ICSR[];

  editForm = this.fb.group({
    id: [],
    attributeType: [null, [Validators.required]],
    holdingRequestAttribute: [],
    csr: []
  });

  constructor(
    protected jhiAlertService: JhiAlertService,
    protected requestAttributeService: RequestAttributeService,
    protected requestAttributeValueService: RequestAttributeValueService,
    protected cSRService: CSRService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ requestAttribute }) => {
      this.updateForm(requestAttribute);
    });
    this.requestAttributeValueService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<IRequestAttributeValue[]>) => mayBeOk.ok),
        map((response: HttpResponse<IRequestAttributeValue[]>) => response.body)
      )
      .subscribe(
        (res: IRequestAttributeValue[]) => (this.requestattributevalues = res),
        (res: HttpErrorResponse) => this.onError(res.message)
      );
    this.cSRService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<ICSR[]>) => mayBeOk.ok),
        map((response: HttpResponse<ICSR[]>) => response.body)
      )
      .subscribe((res: ICSR[]) => (this.csrs = res), (res: HttpErrorResponse) => this.onError(res.message));
  }

  updateForm(requestAttribute: IRequestAttribute) {
    this.editForm.patchValue({
      id: requestAttribute.id,
      attributeType: requestAttribute.attributeType,
      holdingRequestAttribute: requestAttribute.holdingRequestAttribute,
      csr: requestAttribute.csr
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const requestAttribute = this.createFromForm();
    if (requestAttribute.id !== undefined) {
      this.subscribeToSaveResponse(this.requestAttributeService.update(requestAttribute));
    } else {
      this.subscribeToSaveResponse(this.requestAttributeService.create(requestAttribute));
    }
  }

  private createFromForm(): IRequestAttribute {
    return {
      ...new RequestAttribute(),
      id: this.editForm.get(['id']).value,
      attributeType: this.editForm.get(['attributeType']).value,
      holdingRequestAttribute: this.editForm.get(['holdingRequestAttribute']).value,
      csr: this.editForm.get(['csr']).value
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IRequestAttribute>>) {
    result.subscribe(() => this.onSaveSuccess(), () => this.onSaveError());
  }

  protected onSaveSuccess() {
    this.isSaving = false;
    this.previousState();
  }

  protected onSaveError() {
    this.isSaving = false;
  }
  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }

  trackRequestAttributeValueById(index: number, item: IRequestAttributeValue) {
    return item.id;
  }

  trackCSRById(index: number, item: ICSR) {
    return item.id;
  }
}
