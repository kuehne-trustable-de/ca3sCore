<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="identifier">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.identifier.detail.title')">Identifier</span> {{identifier.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.identifier.identifierId')">Identifier Id</span>
                    </dt>
                    <dd>
                        <span>{{identifier.identifierId}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.identifier.type')">Type</span>
                    </dt>
                    <dd>
                        <span>{{identifier.type}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.identifier.value')">Value</span>
                    </dt>
                    <dd>
                        <span>{{identifier.value}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.identifier.order')">Order</span>
                    </dt>
                    <dd>
                        <div v-if="identifier.order">
                            <router-link :to="{name: 'AcmeOrderView', params: {acmeOrderId: identifier.order.id}}">{{identifier.order.id}}</router-link>
                        </div>
                    </dd>
                </dl>
                <button type="submit"
                        v-on:click.prevent="previousState()"
                        class="btn btn-info">
                    <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                </button>
                <router-link v-if="identifier.id" :to="{name: 'IdentifierEdit', params: {identifierId: identifier.id}}" tag="button" class="btn btn-primary">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
                </router-link>
            </div>
        </div>
    </div>
</template>

<script lang="ts" src="./identifier-details.component.ts">
</script>
