import { Component, OnInit } from '@angular/core';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { JhiAlertService, JhiDataUtils } from 'ng-jhipster';
import { IACMEAccount, ACMEAccount } from 'app/shared/model/acme-account.model';
import { ACMEAccountService } from './acme-account.service';

@Component({
  selector: 'jhi-acme-account-update',
  templateUrl: './acme-account-update.component.html'
})
export class ACMEAccountUpdateComponent implements OnInit {
  isSaving: boolean;

  editForm = this.fb.group({
    id: [],
    accountId: [null, [Validators.required]],
    realm: [null, [Validators.required]],
    status: [],
    termsOfServiceAgreed: [null, [Validators.required]],
    publicKeyHash: [null, [Validators.required]],
    publicKey: [null, [Validators.required]]
  });

  constructor(
    protected dataUtils: JhiDataUtils,
    protected jhiAlertService: JhiAlertService,
    protected aCMEAccountService: ACMEAccountService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ aCMEAccount }) => {
      this.updateForm(aCMEAccount);
    });
  }

  updateForm(aCMEAccount: IACMEAccount) {
    this.editForm.patchValue({
      id: aCMEAccount.id,
      accountId: aCMEAccount.accountId,
      realm: aCMEAccount.realm,
      status: aCMEAccount.status,
      termsOfServiceAgreed: aCMEAccount.termsOfServiceAgreed,
      publicKeyHash: aCMEAccount.publicKeyHash,
      publicKey: aCMEAccount.publicKey
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
    const aCMEAccount = this.createFromForm();
    if (aCMEAccount.id !== undefined) {
      this.subscribeToSaveResponse(this.aCMEAccountService.update(aCMEAccount));
    } else {
      this.subscribeToSaveResponse(this.aCMEAccountService.create(aCMEAccount));
    }
  }

  private createFromForm(): IACMEAccount {
    return {
      ...new ACMEAccount(),
      id: this.editForm.get(['id']).value,
      accountId: this.editForm.get(['accountId']).value,
      realm: this.editForm.get(['realm']).value,
      status: this.editForm.get(['status']).value,
      termsOfServiceAgreed: this.editForm.get(['termsOfServiceAgreed']).value,
      publicKeyHash: this.editForm.get(['publicKeyHash']).value,
      publicKey: this.editForm.get(['publicKey']).value
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IACMEAccount>>) {
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
