<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="certificateAttribute">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.certificateAttribute.detail.title')">CertificateAttribute</span> {{certificateAttribute.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.certificateAttribute.name')">Name</span>
                    </dt>
                    <dd>
                        <span>{{certificateAttribute.name}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificateAttribute.value')">Value</span>
                    </dt>
                    <dd>
                        <span>{{certificateAttribute.value}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.certificateAttribute.certificate')">Certificate</span>
                    </dt>
                    <dd>
                        <div v-if="certificateAttribute.certificate">
                            <router-link :to="{name: 'CertificateView', params: {certificateId: certificateAttribute.certificate.id}}">{{certificateAttribute.certificate.id}}</router-link>
                        </div>
                    </dd>
                </dl>
                <button type="submit"
                        v-on:click.prevent="previousState()"
                        class="btn btn-info">
                    <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                </button>
                <router-link v-if="certificateAttribute.id" :to="{name: 'CertificateAttributeEdit', params: {certificateAttributeId: certificateAttribute.id}}" tag="button" class="btn btn-primary">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
                </router-link>
            </div>
        </div>
    </div>
</template>

<script lang="ts" src="./certificate-attribute-details.component.ts">
</script>
