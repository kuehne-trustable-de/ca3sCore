<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="protectedContent">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.protectedContent.detail.title')">ProtectedContent</span> {{protectedContent.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.protectedContent.contentBase64')">Content Base 64</span>
                    </dt>
                    <dd>
                        <span>{{protectedContent.contentBase64}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.protectedContent.type')">Type</span>
                    </dt>
                    <dd>
                        <span v-text="$t('ca3SApp.ProtectedContentType.' + protectedContent.type)">{{protectedContent.type}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.protectedContent.relationType')">Relation Type</span>
                    </dt>
                    <dd>
                        <span v-text="$t('ca3SApp.ContentRelationType.' + protectedContent.relationType)">{{protectedContent.relationType}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.protectedContent.relatedId')">Related Id</span>
                    </dt>
                    <dd>
                        <span>{{protectedContent.relatedId}}</span>
                    </dd>
                </dl>
                <button type="submit"
                        v-on:click.prevent="previousState()"
                        class="btn btn-info">
                    <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                </button>
                
                <router-link v-if="protectedContent.id" :to="{name: 'ProtectedContentEdit', params: {protectedContentId: protectedContent.id}}" tag="button" class="btn btn-primary">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
                </router-link>
            </div>
        </div>
    </div>
</template>

<script lang="ts" src="./protected-content-details.component.ts">
</script>
