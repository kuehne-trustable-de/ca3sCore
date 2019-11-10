import { Component, OnInit } from '@angular/core';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import * as moment from 'moment';
import { INonce, Nonce } from 'app/shared/model/nonce.model';
import { NonceService } from './nonce.service';

@Component({
  selector: 'jhi-nonce-update',
  templateUrl: './nonce-update.component.html'
})
export class NonceUpdateComponent implements OnInit {
  isSaving: boolean;
  expiresAtDp: any;

  editForm = this.fb.group({
    id: [],
    nonceValue: [],
    expiresAt: []
  });

  constructor(protected nonceService: NonceService, protected activatedRoute: ActivatedRoute, private fb: FormBuilder) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ nonce }) => {
      this.updateForm(nonce);
    });
  }

  updateForm(nonce: INonce) {
    this.editForm.patchValue({
      id: nonce.id,
      nonceValue: nonce.nonceValue,
      expiresAt: nonce.expiresAt
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const nonce = this.createFromForm();
    if (nonce.id !== undefined) {
      this.subscribeToSaveResponse(this.nonceService.update(nonce));
    } else {
      this.subscribeToSaveResponse(this.nonceService.create(nonce));
    }
  }

  private createFromForm(): INonce {
    return {
      ...new Nonce(),
      id: this.editForm.get(['id']).value,
      nonceValue: this.editForm.get(['nonceValue']).value,
      expiresAt: this.editForm.get(['expiresAt']).value
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<INonce>>) {
    result.subscribe(() => this.onSaveSuccess(), () => this.onSaveError());
  }

  protected onSaveSuccess() {
    this.isSaving = false;
    this.previousState();
  }

  protected onSaveError() {
    this.isSaving = false;
  }
}
