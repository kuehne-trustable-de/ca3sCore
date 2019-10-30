import { Component, OnInit } from '@angular/core';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { ISelectorToTemplate, SelectorToTemplate } from 'app/shared/model/selector-to-template.model';
import { SelectorToTemplateService } from './selector-to-template.service';

@Component({
  selector: 'jhi-selector-to-template-update',
  templateUrl: './selector-to-template-update.component.html'
})
export class SelectorToTemplateUpdateComponent implements OnInit {
  isSaving: boolean;

  editForm = this.fb.group({
    id: [],
    selector: [null, [Validators.required]],
    template: [null, [Validators.required]],
    comment: []
  });

  constructor(
    protected selectorToTemplateService: SelectorToTemplateService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ selectorToTemplate }) => {
      this.updateForm(selectorToTemplate);
    });
  }

  updateForm(selectorToTemplate: ISelectorToTemplate) {
    this.editForm.patchValue({
      id: selectorToTemplate.id,
      selector: selectorToTemplate.selector,
      template: selectorToTemplate.template,
      comment: selectorToTemplate.comment
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const selectorToTemplate = this.createFromForm();
    if (selectorToTemplate.id !== undefined) {
      this.subscribeToSaveResponse(this.selectorToTemplateService.update(selectorToTemplate));
    } else {
      this.subscribeToSaveResponse(this.selectorToTemplateService.create(selectorToTemplate));
    }
  }

  private createFromForm(): ISelectorToTemplate {
    return {
      ...new SelectorToTemplate(),
      id: this.editForm.get(['id']).value,
      selector: this.editForm.get(['selector']).value,
      template: this.editForm.get(['template']).value,
      comment: this.editForm.get(['comment']).value
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ISelectorToTemplate>>) {
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
