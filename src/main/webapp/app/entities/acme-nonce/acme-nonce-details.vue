<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="acmeNonce">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.acmeNonce.detail.title')">AcmeNonce</span> {{acmeNonce.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.acmeNonce.nonceValue')">Nonce Value</span>
                    </dt>
                    <dd>
                        <span>{{acmeNonce.nonceValue}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.acmeNonce.expiresAt')">Expires At</span>
                    </dt>
                    <dd>
                        <span v-if="acmeNonce.expiresAt">{{$d(Date.parse(acmeNonce.expiresAt), 'long') }}</span>
                    </dd>
                </dl>
                <button type="submit"
                        v-on:click.prevent="previousState()"
                        class="btn btn-info">
                    <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                </button>
                
                <router-link v-if="acmeNonce.id" :to="{name: 'AcmeNonceEdit', params: {acmeNonceId: acmeNonce.id}}" tag="button" class="btn btn-primary">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
                </router-link>
            </div>
        </div>
    </div>
</template>

<script lang="ts" src="./acme-nonce-details.component.ts">
</script>
