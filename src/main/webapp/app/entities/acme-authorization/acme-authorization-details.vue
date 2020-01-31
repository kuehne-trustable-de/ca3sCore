<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="acmeAuthorization">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.acmeAuthorization.detail.title')">AcmeAuthorization</span> {{acmeAuthorization.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.acmeAuthorization.acmeAuthorizationId')">Acme Authorization Id</span>
                    </dt>
                    <dd>
                        <span>{{acmeAuthorization.acmeAuthorizationId}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.acmeAuthorization.type')">Type</span>
                    </dt>
                    <dd>
                        <span>{{acmeAuthorization.type}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.acmeAuthorization.value')">Value</span>
                    </dt>
                    <dd>
                        <span>{{acmeAuthorization.value}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.acmeAuthorization.order')">Order</span>
                    </dt>
                    <dd>
                        <div v-if="acmeAuthorization.order">
                            <router-link :to="{name: 'AcmeOrderView', params: {acmeOrderId: acmeAuthorization.order.id}}">{{acmeAuthorization.order.id}}</router-link>
                        </div>
                    </dd>
                </dl>
                <button type="submit"
                        v-on:click.prevent="previousState()"
                        class="btn btn-info">
                    <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                </button>
                
                <router-link v-if="acmeAuthorization.id" :to="{name: 'AcmeAuthorizationEdit', params: {acmeAuthorizationId: acmeAuthorization.id}}" tag="button" class="btn btn-primary">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
                </router-link>
            </div>
        </div>
    </div>
</template>

<script lang="ts" src="./acme-authorization-details.component.ts">
</script>
