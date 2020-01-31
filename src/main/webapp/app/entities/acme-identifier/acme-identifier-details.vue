<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="acmeIdentifier">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.acmeIdentifier.detail.title')">AcmeIdentifier</span> {{acmeIdentifier.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.acmeIdentifier.acmeIdentifierId')">Acme Identifier Id</span>
                    </dt>
                    <dd>
                        <span>{{acmeIdentifier.acmeIdentifierId}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.acmeIdentifier.type')">Type</span>
                    </dt>
                    <dd>
                        <span>{{acmeIdentifier.type}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.acmeIdentifier.value')">Value</span>
                    </dt>
                    <dd>
                        <span>{{acmeIdentifier.value}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.acmeIdentifier.order')">Order</span>
                    </dt>
                    <dd>
                        <div v-if="acmeIdentifier.order">
                            <router-link :to="{name: 'AcmeOrderView', params: {acmeOrderId: acmeIdentifier.order.id}}">{{acmeIdentifier.order.id}}</router-link>
                        </div>
                    </dd>
                </dl>
                <button type="submit"
                        v-on:click.prevent="previousState()"
                        class="btn btn-info">
                    <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                </button>
                
                <router-link v-if="acmeIdentifier.id" :to="{name: 'AcmeIdentifierEdit', params: {acmeIdentifierId: acmeIdentifier.id}}" tag="button" class="btn btn-primary">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
                </router-link>
            </div>
        </div>
    </div>
</template>

<script lang="ts" src="./acme-identifier-details.component.ts">
</script>
