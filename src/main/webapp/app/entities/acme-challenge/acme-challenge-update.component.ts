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
import { IAcmeChallenge, AcmeChallenge } from 'app/shared/model/acme-challenge.model';
import { AcmeChallengeService } from './acme-challenge.service';
import { IAuthorization } from 'app/shared/model/authorization.model';
import { AuthorizationService } from 'app/entities/authorization/authorization.service';

@Component({
  selector: 'jhi-acme-challenge-update',
  templateUrl: './acme-challenge-update.component.html'
})
export class AcmeChallengeUpdateComponent implements OnInit {
  isSaving: boolean;

  authorizations: IAuthorization[];
  validatedDp: any;

  editForm = this.fb.group({
    id: [],
    challengeId: [null, [Validators.required]],
    type: [null, [Validators.required]],
    value: [null, [Validators.required]],
    token: [null, [Validators.required]],
    validated: [],
    status: [null, [Validators.required]],
    authorization: []
  });

  constructor(
    protected jhiAlertService: JhiAlertService,
    protected acmeChallengeService: AcmeChallengeService,
    protected authorizationService: AuthorizationService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ acmeChallenge }) => {
      this.updateForm(acmeChallenge);
    });
    this.authorizationService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<IAuthorization[]>) => mayBeOk.ok),
        map((response: HttpResponse<IAuthorization[]>) => response.body)
      )
      .subscribe((res: IAuthorization[]) => (this.authorizations = res), (res: HttpErrorResponse) => this.onError(res.message));
  }

  updateForm(acmeChallenge: IAcmeChallenge) {
    this.editForm.patchValue({
      id: acmeChallenge.id,
      challengeId: acmeChallenge.challengeId,
      type: acmeChallenge.type,
      value: acmeChallenge.value,
      token: acmeChallenge.token,
      validated: acmeChallenge.validated,
      status: acmeChallenge.status,
      authorization: acmeChallenge.authorization
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const acmeChallenge = this.createFromForm();
    if (acmeChallenge.id !== undefined) {
      this.subscribeToSaveResponse(this.acmeChallengeService.update(acmeChallenge));
    } else {
      this.subscribeToSaveResponse(this.acmeChallengeService.create(acmeChallenge));
    }
  }

  private createFromForm(): IAcmeChallenge {
    return {
      ...new AcmeChallenge(),
      id: this.editForm.get(['id']).value,
      challengeId: this.editForm.get(['challengeId']).value,
      type: this.editForm.get(['type']).value,
      value: this.editForm.get(['value']).value,
      token: this.editForm.get(['token']).value,
      validated: this.editForm.get(['validated']).value,
      status: this.editForm.get(['status']).value,
      authorization: this.editForm.get(['authorization']).value
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IAcmeChallenge>>) {
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

  trackAuthorizationById(index: number, item: IAuthorization) {
    return item.id;
  }
}
