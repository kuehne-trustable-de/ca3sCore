import { Component, OnInit } from '@angular/core';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import * as moment from 'moment';
import { JhiAlertService } from 'ng-jhipster';
import { IAcmeOrder, AcmeOrder } from 'app/shared/model/acme-order.model';
import { AcmeOrderService } from './acme-order.service';
import { ICSR } from 'app/shared/model/csr.model';
import { CSRService } from 'app/entities/csr/csr.service';
import { ICertificate } from 'app/shared/model/certificate.model';
import { CertificateService } from 'app/entities/certificate/certificate.service';
import { IACMEAccount } from 'app/shared/model/acme-account.model';
import { ACMEAccountService } from 'app/entities/acme-account/acme-account.service';

@Component({
  selector: 'jhi-acme-order-update',
  templateUrl: './acme-order-update.component.html'
})
export class AcmeOrderUpdateComponent implements OnInit {
  isSaving: boolean;

  csrs: ICSR[];

  certificates: ICertificate[];

  acmeaccounts: IACMEAccount[];
  expiresDp: any;
  notBeforeDp: any;
  notAfterDp: any;

  editForm = this.fb.group({
    id: [],
    status: [null, [Validators.required]],
    expires: [],
    notBefore: [],
    notAfter: [],
    error: [],
    finalizeUrl: [],
    certificateUrl: [],
    csr: [],
    certificate: [],
    account: []
  });

  constructor(
    protected jhiAlertService: JhiAlertService,
    protected acmeOrderService: AcmeOrderService,
    protected cSRService: CSRService,
    protected certificateService: CertificateService,
    protected aCMEAccountService: ACMEAccountService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ acmeOrder }) => {
      this.updateForm(acmeOrder);
    });
    this.cSRService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<ICSR[]>) => mayBeOk.ok),
        map((response: HttpResponse<ICSR[]>) => response.body)
      )
      .subscribe((res: ICSR[]) => (this.csrs = res), (res: HttpErrorResponse) => this.onError(res.message));
    this.certificateService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<ICertificate[]>) => mayBeOk.ok),
        map((response: HttpResponse<ICertificate[]>) => response.body)
      )
      .subscribe((res: ICertificate[]) => (this.certificates = res), (res: HttpErrorResponse) => this.onError(res.message));
    this.aCMEAccountService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<IACMEAccount[]>) => mayBeOk.ok),
        map((response: HttpResponse<IACMEAccount[]>) => response.body)
      )
      .subscribe((res: IACMEAccount[]) => (this.acmeaccounts = res), (res: HttpErrorResponse) => this.onError(res.message));
  }

  updateForm(acmeOrder: IAcmeOrder) {
    this.editForm.patchValue({
      id: acmeOrder.id,
      status: acmeOrder.status,
      expires: acmeOrder.expires,
      notBefore: acmeOrder.notBefore,
      notAfter: acmeOrder.notAfter,
      error: acmeOrder.error,
      finalizeUrl: acmeOrder.finalizeUrl,
      certificateUrl: acmeOrder.certificateUrl,
      csr: acmeOrder.csr,
      certificate: acmeOrder.certificate,
      account: acmeOrder.account
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const acmeOrder = this.createFromForm();
    if (acmeOrder.id !== undefined) {
      this.subscribeToSaveResponse(this.acmeOrderService.update(acmeOrder));
    } else {
      this.subscribeToSaveResponse(this.acmeOrderService.create(acmeOrder));
    }
  }

  private createFromForm(): IAcmeOrder {
    return {
      ...new AcmeOrder(),
      id: this.editForm.get(['id']).value,
      status: this.editForm.get(['status']).value,
      expires: this.editForm.get(['expires']).value,
      notBefore: this.editForm.get(['notBefore']).value,
      notAfter: this.editForm.get(['notAfter']).value,
      error: this.editForm.get(['error']).value,
      finalizeUrl: this.editForm.get(['finalizeUrl']).value,
      certificateUrl: this.editForm.get(['certificateUrl']).value,
      csr: this.editForm.get(['csr']).value,
      certificate: this.editForm.get(['certificate']).value,
      account: this.editForm.get(['account']).value
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IAcmeOrder>>) {
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

  trackCertificateById(index: number, item: ICertificate) {
    return item.id;
  }

  trackACMEAccountById(index: number, item: IACMEAccount) {
    return item.id;
  }
}
