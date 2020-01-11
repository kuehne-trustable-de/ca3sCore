<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="nonce">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.nonce.detail.title')">Nonce</span> {{nonce.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.nonce.nonceValue')">Nonce Value</span>
                    </dt>
                    <dd>
                        <span>{{nonce.nonceValue}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.nonce.expiresAt')">Expires At</span>
                    </dt>
                    <dd>
                        <span>{{nonce.expiresAt}}</span>
                    </dd>
                </dl>
                <button type="submit"
                        v-on:click.prevent="previousState()"
                        class="btn btn-info">
                    <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                </button>
                <router-link v-if="nonce.id" :to="{name: 'NonceEdit', params: {nonceId: nonce.id}}" tag="button" class="btn btn-primary">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
                </router-link>
            </div>
        </div>
    </div>
</template>

<script lang="ts" src="./nonce-details.component.ts">
</script>
