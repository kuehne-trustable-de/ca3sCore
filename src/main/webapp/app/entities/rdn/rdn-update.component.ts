import { Component, OnInit } from '@angular/core';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { JhiAlertService } from 'ng-jhipster';
import { IRDN, RDN } from 'app/shared/model/rdn.model';
import { RDNService } from './rdn.service';
import { ICSR } from 'app/shared/model/csr.model';
import { CSRService } from 'app/entities/csr/csr.service';

@Component({
  selector: 'jhi-rdn-update',
  templateUrl: './rdn-update.component.html'
})
export class RDNUpdateComponent implements OnInit {
  isSaving: boolean;

  csrs: ICSR[];

  editForm = this.fb.group({
    id: [],
    csr: []
  });

  constructor(
    protected jhiAlertService: JhiAlertService,
    protected rDNService: RDNService,
    protected cSRService: CSRService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ rDN }) => {
      this.updateForm(rDN);
    });
    this.cSRService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<ICSR[]>) => mayBeOk.ok),
        map((response: HttpResponse<ICSR[]>) => response.body)
      )
      .subscribe((res: ICSR[]) => (this.csrs = res), (res: HttpErrorResponse) => this.onError(res.message));
  }

  updateForm(rDN: IRDN) {
    this.editForm.patchValue({
      id: rDN.id,
      csr: rDN.csr
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const rDN = this.createFromForm();
    if (rDN.id !== undefined) {
      this.subscribeToSaveResponse(this.rDNService.update(rDN));
    } else {
      this.subscribeToSaveResponse(this.rDNService.create(rDN));
    }
  }

  private createFromForm(): IRDN {
    return {
      ...new RDN(),
      id: this.editForm.get(['id']).value,
      csr: this.editForm.get(['csr']).value
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IRDN>>) {
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
