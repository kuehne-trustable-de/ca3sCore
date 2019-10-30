import { Component, OnInit } from '@angular/core';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import * as moment from 'moment';
import { JhiAlertService, JhiDataUtils } from 'ng-jhipster';
import { ICSR, CSR } from 'app/shared/model/csr.model';
import { CSRService } from './csr.service';

@Component({
  selector: 'jhi-csr-update',
  templateUrl: './csr-update.component.html'
})
export class CSRUpdateComponent implements OnInit {
  isSaving: boolean;
  requestedOnDp: any;

  editForm = this.fb.group({
    id: [],
    csrBase64: [null, [Validators.required]],
    requestedOn: [null, [Validators.required]],
    status: [null, [Validators.required]],
    processInstanceId: [],
    signingAlgorithm: [],
    isCSRValid: [],
    x509KeySpec: [],
    publicKeyAlgorithm: [],
    publicKeyHash: [],
    subjectPublicKeyInfoBase64: [null, [Validators.required]]
  });

  constructor(
    protected dataUtils: JhiDataUtils,
    protected jhiAlertService: JhiAlertService,
    protected cSRService: CSRService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ cSR }) => {
      this.updateForm(cSR);
    });
  }

  updateForm(cSR: ICSR) {
    this.editForm.patchValue({
      id: cSR.id,
      csrBase64: cSR.csrBase64,
      requestedOn: cSR.requestedOn,
      status: cSR.status,
      processInstanceId: cSR.processInstanceId,
      signingAlgorithm: cSR.signingAlgorithm,
      isCSRValid: cSR.isCSRValid,
      x509KeySpec: cSR.x509KeySpec,
      publicKeyAlgorithm: cSR.publicKeyAlgorithm,
      publicKeyHash: cSR.publicKeyHash,
      subjectPublicKeyInfoBase64: cSR.subjectPublicKeyInfoBase64
    });
  }

  byteSize(field) {
    return this.dataUtils.byteSize(field);
  }

  openFile(contentType, field) {
    return this.dataUtils.openFile(contentType, field);
  }

  setFileData(event, field: string, isImage) {
    return new Promise((resolve, reject) => {
      if (event && event.target && event.target.files && event.target.files[0]) {
        const file: File = event.target.files[0];
        if (isImage && !file.type.startsWith('image/')) {
          reject(`File was expected to be an image but was found to be ${file.type}`);
        } else {
          const filedContentType: string = field + 'ContentType';
          this.dataUtils.toBase64(file, base64Data => {
            this.editForm.patchValue({
              [field]: base64Data,
              [filedContentType]: file.type
            });
          });
        }
      } else {
        reject(`Base64 data was not set as file could not be extracted from passed parameter: ${event}`);
      }
    }).then(
      // eslint-disable-next-line no-console
      () => console.log('blob added'), // success
      this.onError
    );
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const cSR = this.createFromForm();
    if (cSR.id !== undefined) {
      this.subscribeToSaveResponse(this.cSRService.update(cSR));
    } else {
      this.subscribeToSaveResponse(this.cSRService.create(cSR));
    }
  }

  private createFromForm(): ICSR {
    return {
      ...new CSR(),
      id: this.editForm.get(['id']).value,
      csrBase64: this.editForm.get(['csrBase64']).value,
      requestedOn: this.editForm.get(['requestedOn']).value,
      status: this.editForm.get(['status']).value,
      processInstanceId: this.editForm.get(['processInstanceId']).value,
      signingAlgorithm: this.editForm.get(['signingAlgorithm']).value,
      isCSRValid: this.editForm.get(['isCSRValid']).value,
      x509KeySpec: this.editForm.get(['x509KeySpec']).value,
      publicKeyAlgorithm: this.editForm.get(['publicKeyAlgorithm']).value,
      publicKeyHash: this.editForm.get(['publicKeyHash']).value,
      subjectPublicKeyInfoBase64: this.editForm.get(['subjectPublicKeyInfoBase64']).value
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ICSR>>) {
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
}
