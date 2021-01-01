<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="importedURL">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.importedURL.detail.title')">ImportedURL</span> {{importedURL.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.importedURL.name')">Name</span>
                    </dt>
                    <dd>
                        <span>{{importedURL.name}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.importedURL.importDate')">Import Date</span>
                    </dt>
                    <dd>
                        <span v-if="importedURL.importDate">{{$d(Date.parse(importedURL.importDate), 'long') }}</span>
                    </dd>
                </dl>
                <button type="submit"
                        v-on:click.prevent="previousState()"
                        class="btn btn-info">
                    <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                </button>
                <router-link v-if="importedURL.id" :to="{name: 'ImportedURLEdit', params: {importedURLId: importedURL.id}}" tag="button" class="btn btn-primary">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
                </router-link>
            </div>
        </div>
    </div>
</template>

<script lang="ts" src="./imported-url-details.component.ts">
</script>
