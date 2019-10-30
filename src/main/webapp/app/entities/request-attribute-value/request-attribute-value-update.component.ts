import { Component, OnInit } from '@angular/core';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { JhiAlertService } from 'ng-jhipster';
import { IRequestAttributeValue, RequestAttributeValue } from 'app/shared/model/request-attribute-value.model';
import { RequestAttributeValueService } from './request-attribute-value.service';
import { IRequestAttribute } from 'app/shared/model/request-attribute.model';
import { RequestAttributeService } from 'app/entities/request-attribute/request-attribute.service';

@Component({
  selector: 'jhi-request-attribute-value-update',
  templateUrl: './request-attribute-value-update.component.html'
})
export class RequestAttributeValueUpdateComponent implements OnInit {
  isSaving: boolean;

  requestattributes: IRequestAttribute[];

  editForm = this.fb.group({
    id: [],
    attributeValue: [null, [Validators.required]],
    reqAttr: []
  });

  constructor(
    protected jhiAlertService: JhiAlertService,
    protected requestAttributeValueService: RequestAttributeValueService,
    protected requestAttributeService: RequestAttributeService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ requestAttributeValue }) => {
      this.updateForm(requestAttributeValue);
    });
    this.requestAttributeService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<IRequestAttribute[]>) => mayBeOk.ok),
        map((response: HttpResponse<IRequestAttribute[]>) => response.body)
      )
      .subscribe((res: IRequestAttribute[]) => (this.requestattributes = res), (res: HttpErrorResponse) => this.onError(res.message));
  }

  updateForm(requestAttributeValue: IRequestAttributeValue) {
    this.editForm.patchValue({
      id: requestAttributeValue.id,
      attributeValue: requestAttributeValue.attributeValue,
      reqAttr: requestAttributeValue.reqAttr
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const requestAttributeValue = this.createFromForm();
    if (requestAttributeValue.id !== undefined) {
      this.subscribeToSaveResponse(this.requestAttributeValueService.update(requestAttributeValue));
    } else {
      this.subscribeToSaveResponse(this.requestAttributeValueService.create(requestAttributeValue));
    }
  }

  private createFromForm(): IRequestAttributeValue {
    return {
      ...new RequestAttributeValue(),
      id: this.editForm.get(['id']).value,
      attributeValue: this.editForm.get(['attributeValue']).value,
      reqAttr: this.editForm.get(['reqAttr']).value
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IRequestAttributeValue>>) {
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

  trackRequestAttributeById(index: number, item: IRequestAttribute) {
    return item.id;
  }
}
