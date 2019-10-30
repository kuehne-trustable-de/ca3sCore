import { Component, OnInit } from '@angular/core';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { JhiAlertService } from 'ng-jhipster';
import { IRDNAttribute, RDNAttribute } from 'app/shared/model/rdn-attribute.model';
import { RDNAttributeService } from './rdn-attribute.service';
import { IRDN } from 'app/shared/model/rdn.model';
import { RDNService } from 'app/entities/rdn/rdn.service';

@Component({
  selector: 'jhi-rdn-attribute-update',
  templateUrl: './rdn-attribute-update.component.html'
})
export class RDNAttributeUpdateComponent implements OnInit {
  isSaving: boolean;

  rdns: IRDN[];

  editForm = this.fb.group({
    id: [],
    attributeType: [null, [Validators.required]],
    attributeValue: [null, [Validators.required]],
    rdn: []
  });

  constructor(
    protected jhiAlertService: JhiAlertService,
    protected rDNAttributeService: RDNAttributeService,
    protected rDNService: RDNService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ rDNAttribute }) => {
      this.updateForm(rDNAttribute);
    });
    this.rDNService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<IRDN[]>) => mayBeOk.ok),
        map((response: HttpResponse<IRDN[]>) => response.body)
      )
      .subscribe((res: IRDN[]) => (this.rdns = res), (res: HttpErrorResponse) => this.onError(res.message));
  }

  updateForm(rDNAttribute: IRDNAttribute) {
    this.editForm.patchValue({
      id: rDNAttribute.id,
      attributeType: rDNAttribute.attributeType,
      attributeValue: rDNAttribute.attributeValue,
      rdn: rDNAttribute.rdn
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const rDNAttribute = this.createFromForm();
    if (rDNAttribute.id !== undefined) {
      this.subscribeToSaveResponse(this.rDNAttributeService.update(rDNAttribute));
    } else {
      this.subscribeToSaveResponse(this.rDNAttributeService.create(rDNAttribute));
    }
  }

  private createFromForm(): IRDNAttribute {
    return {
      ...new RDNAttribute(),
      id: this.editForm.get(['id']).value,
      attributeType: this.editForm.get(['attributeType']).value,
      attributeValue: this.editForm.get(['attributeValue']).value,
      rdn: this.editForm.get(['rdn']).value
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IRDNAttribute>>) {
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

  trackRDNById(index: number, item: IRDN) {
    return item.id;
  }
}
