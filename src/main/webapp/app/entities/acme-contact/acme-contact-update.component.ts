import { Component, OnInit } from '@angular/core';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { JhiAlertService } from 'ng-jhipster';
import { IAcmeContact, AcmeContact } from 'app/shared/model/acme-contact.model';
import { AcmeContactService } from './acme-contact.service';
import { IACMEAccount } from 'app/shared/model/acme-account.model';
import { ACMEAccountService } from 'app/entities/acme-account/acme-account.service';

@Component({
  selector: 'jhi-acme-contact-update',
  templateUrl: './acme-contact-update.component.html'
})
export class AcmeContactUpdateComponent implements OnInit {
  isSaving: boolean;

  acmeaccounts: IACMEAccount[];

  editForm = this.fb.group({
    id: [],
    contactId: [null, [Validators.required]],
    contactUrl: [null, [Validators.required]],
    account: []
  });

  constructor(
    protected jhiAlertService: JhiAlertService,
    protected acmeContactService: AcmeContactService,
    protected aCMEAccountService: ACMEAccountService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ acmeContact }) => {
      this.updateForm(acmeContact);
    });
    this.aCMEAccountService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<IACMEAccount[]>) => mayBeOk.ok),
        map((response: HttpResponse<IACMEAccount[]>) => response.body)
      )
      .subscribe((res: IACMEAccount[]) => (this.acmeaccounts = res), (res: HttpErrorResponse) => this.onError(res.message));
  }

  updateForm(acmeContact: IAcmeContact) {
    this.editForm.patchValue({
      id: acmeContact.id,
      contactId: acmeContact.contactId,
      contactUrl: acmeContact.contactUrl,
      account: acmeContact.account
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const acmeContact = this.createFromForm();
    if (acmeContact.id !== undefined) {
      this.subscribeToSaveResponse(this.acmeContactService.update(acmeContact));
    } else {
      this.subscribeToSaveResponse(this.acmeContactService.create(acmeContact));
    }
  }

  private createFromForm(): IAcmeContact {
    return {
      ...new AcmeContact(),
      id: this.editForm.get(['id']).value,
      contactId: this.editForm.get(['contactId']).value,
      contactUrl: this.editForm.get(['contactUrl']).value,
      account: this.editForm.get(['account']).value
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IAcmeContact>>) {
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

  trackACMEAccountById(index: number, item: IACMEAccount) {
    return item.id;
  }
}
