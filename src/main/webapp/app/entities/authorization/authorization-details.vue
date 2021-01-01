<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="authorization">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.authorization.detail.title')">Authorization</span> {{authorization.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.authorization.authorizationId')">Authorization Id</span>
                    </dt>
                    <dd>
                        <span>{{authorization.authorizationId}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.authorization.type')">Type</span>
                    </dt>
                    <dd>
                        <span>{{authorization.type}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.authorization.value')">Value</span>
                    </dt>
                    <dd>
                        <span>{{authorization.value}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.authorization.order')">Order</span>
                    </dt>
                    <dd>
                        <div v-if="authorization.order">
                            <router-link :to="{name: 'AcmeOrderView', params: {acmeOrderId: authorization.order.id}}">{{authorization.order.id}}</router-link>
                        </div>
                    </dd>
                </dl>
                <button type="submit"
                        v-on:click.prevent="previousState()"
                        class="btn btn-info">
                    <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                </button>
                <router-link v-if="authorization.id" :to="{name: 'AuthorizationEdit', params: {authorizationId: authorization.id}}" tag="button" class="btn btn-primary">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
                </router-link>
            </div>
        </div>
    </div>
</template>

<script lang="ts" src="./authorization-details.component.ts">
</script>
