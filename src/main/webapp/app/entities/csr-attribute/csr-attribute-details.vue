<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="csrAttribute">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.csrAttribute.detail.title')">CsrAttribute</span> {{csrAttribute.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.csrAttribute.name')">Name</span>
                    </dt>
                    <dd>
                        <span>{{csrAttribute.name}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.csrAttribute.value')">Value</span>
                    </dt>
                    <dd>
                        <span>{{csrAttribute.value}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.csrAttribute.csr')">Csr</span>
                    </dt>
                    <dd>
                        <div v-if="csrAttribute.csr">
                            <router-link :to="{name: 'CSRView', params: {cSRId: csrAttribute.csr.id}}">{{csrAttribute.csr.id}}</router-link>
                        </div>
                    </dd>
                </dl>
                <button type="submit"
                        v-on:click.prevent="previousState()"
                        class="btn btn-info">
                    <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                </button>
                <router-link v-if="csrAttribute.id" :to="{name: 'CsrAttributeEdit', params: {csrAttributeId: csrAttribute.id}}" tag="button" class="btn btn-primary">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
                </router-link>
            </div>
        </div>
    </div>
</template>

<script lang="ts" src="./csr-attribute-details.component.ts">
</script>
