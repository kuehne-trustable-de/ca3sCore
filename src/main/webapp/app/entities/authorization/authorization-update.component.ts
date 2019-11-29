import { Component, OnInit } from '@angular/core';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { JhiAlertService } from 'ng-jhipster';
import { IAuthorization, Authorization } from 'app/shared/model/authorization.model';
import { AuthorizationService } from './authorization.service';
import { IAcmeOrder } from 'app/shared/model/acme-order.model';
import { AcmeOrderService } from 'app/entities/acme-order/acme-order.service';

@Component({
  selector: 'jhi-authorization-update',
  templateUrl: './authorization-update.component.html'
})
export class AuthorizationUpdateComponent implements OnInit {
  isSaving: boolean;

  acmeorders: IAcmeOrder[];

  editForm = this.fb.group({
    id: [],
    authorizationId: [null, [Validators.required]],
    type: [null, [Validators.required]],
    value: [null, [Validators.required]],
    order: []
  });

  constructor(
    protected jhiAlertService: JhiAlertService,
    protected authorizationService: AuthorizationService,
    protected acmeOrderService: AcmeOrderService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ authorization }) => {
      this.updateForm(authorization);
    });
    this.acmeOrderService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<IAcmeOrder[]>) => mayBeOk.ok),
        map((response: HttpResponse<IAcmeOrder[]>) => response.body)
      )
      .subscribe((res: IAcmeOrder[]) => (this.acmeorders = res), (res: HttpErrorResponse) => this.onError(res.message));
  }

  updateForm(authorization: IAuthorization) {
    this.editForm.patchValue({
      id: authorization.id,
      authorizationId: authorization.authorizationId,
      type: authorization.type,
      value: authorization.value,
      order: authorization.order
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const authorization = this.createFromForm();
    if (authorization.id !== undefined) {
      this.subscribeToSaveResponse(this.authorizationService.update(authorization));
    } else {
      this.subscribeToSaveResponse(this.authorizationService.create(authorization));
    }
  }

  private createFromForm(): IAuthorization {
    return {
      ...new Authorization(),
      id: this.editForm.get(['id']).value,
      authorizationId: this.editForm.get(['authorizationId']).value,
      type: this.editForm.get(['type']).value,
      value: this.editForm.get(['value']).value,
      order: this.editForm.get(['order']).value
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IAuthorization>>) {
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

  trackAcmeOrderById(index: number, item: IAcmeOrder) {
    return item.id;
  }
}
