import { Component, OnInit } from '@angular/core';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { JhiAlertService } from 'ng-jhipster';
import { ICertificateAttribute, CertificateAttribute } from 'app/shared/model/certificate-attribute.model';
import { CertificateAttributeService } from './certificate-attribute.service';
import { ICertificate } from 'app/shared/model/certificate.model';
import { CertificateService } from 'app/entities/certificate/certificate.service';

@Component({
  selector: 'jhi-certificate-attribute-update',
  templateUrl: './certificate-attribute-update.component.html'
})
export class CertificateAttributeUpdateComponent implements OnInit {
  isSaving: boolean;

  certificates: ICertificate[];

  editForm = this.fb.group({
    id: [],
    attributeId: [null, [Validators.required]],
    name: [null, [Validators.required]],
    value: [],
    certificate: []
  });

  constructor(
    protected jhiAlertService: JhiAlertService,
    protected certificateAttributeService: CertificateAttributeService,
    protected certificateService: CertificateService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ certificateAttribute }) => {
      this.updateForm(certificateAttribute);
    });
    this.certificateService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<ICertificate[]>) => mayBeOk.ok),
        map((response: HttpResponse<ICertificate[]>) => response.body)
      )
      .subscribe((res: ICertificate[]) => (this.certificates = res), (res: HttpErrorResponse) => this.onError(res.message));
  }

  updateForm(certificateAttribute: ICertificateAttribute) {
    this.editForm.patchValue({
      id: certificateAttribute.id,
      attributeId: certificateAttribute.attributeId,
      name: certificateAttribute.name,
      value: certificateAttribute.value,
      certificate: certificateAttribute.certificate
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const certificateAttribute = this.createFromForm();
    if (certificateAttribute.id !== undefined) {
      this.subscribeToSaveResponse(this.certificateAttributeService.update(certificateAttribute));
    } else {
      this.subscribeToSaveResponse(this.certificateAttributeService.create(certificateAttribute));
    }
  }

  private createFromForm(): ICertificateAttribute {
    return {
      ...new CertificateAttribute(),
      id: this.editForm.get(['id']).value,
      attributeId: this.editForm.get(['attributeId']).value,
      name: this.editForm.get(['name']).value,
      value: this.editForm.get(['value']).value,
      certificate: this.editForm.get(['certificate']).value
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ICertificateAttribute>>) {
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

  trackCertificateById(index: number, item: ICertificate) {
    return item.id;
  }
}
