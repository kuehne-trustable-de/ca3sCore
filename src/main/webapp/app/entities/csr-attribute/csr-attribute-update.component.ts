import { Component, OnInit } from '@angular/core';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { JhiAlertService } from 'ng-jhipster';
import { ICsrAttribute, CsrAttribute } from 'app/shared/model/csr-attribute.model';
import { CsrAttributeService } from './csr-attribute.service';
import { ICSR } from 'app/shared/model/csr.model';
import { CSRService } from 'app/entities/csr/csr.service';

@Component({
  selector: 'jhi-csr-attribute-update',
  templateUrl: './csr-attribute-update.component.html'
})
export class CsrAttributeUpdateComponent implements OnInit {
  isSaving: boolean;

  csrs: ICSR[];

  editForm = this.fb.group({
    id: [],
    name: [null, [Validators.required]],
    value: [null, [Validators.required]],
    csr: []
  });

  constructor(
    protected jhiAlertService: JhiAlertService,
    protected csrAttributeService: CsrAttributeService,
    protected cSRService: CSRService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ csrAttribute }) => {
      this.updateForm(csrAttribute);
    });
    this.cSRService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<ICSR[]>) => mayBeOk.ok),
        map((response: HttpResponse<ICSR[]>) => response.body)
      )
      .subscribe((res: ICSR[]) => (this.csrs = res), (res: HttpErrorResponse) => this.onError(res.message));
  }

  updateForm(csrAttribute: ICsrAttribute) {
    this.editForm.patchValue({
      id: csrAttribute.id,
      name: csrAttribute.name,
      value: csrAttribute.value,
      csr: csrAttribute.csr
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const csrAttribute = this.createFromForm();
    if (csrAttribute.id !== undefined) {
      this.subscribeToSaveResponse(this.csrAttributeService.update(csrAttribute));
    } else {
      this.subscribeToSaveResponse(this.csrAttributeService.create(csrAttribute));
    }
  }

  private createFromForm(): ICsrAttribute {
    return {
      ...new CsrAttribute(),
      id: this.editForm.get(['id']).value,
      name: this.editForm.get(['name']).value,
      value: this.editForm.get(['value']).value,
      csr: this.editForm.get(['csr']).value
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ICsrAttribute>>) {
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

  trackCSRById(index: number, item: ICSR) {
    return item.id;
  }
}
