<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="aCMEAccount">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.aCMEAccount.detail.title')">ACMEAccount</span> {{aCMEAccount.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.aCMEAccount.accountId')">Account Id</span>
                    </dt>
                    <dd>
                        <span>{{aCMEAccount.accountId}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.aCMEAccount.realm')">Realm</span>
                    </dt>
                    <dd>
                        <span>{{aCMEAccount.realm}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.aCMEAccount.status')">Status</span>
                    </dt>
                    <dd>
                        <span v-text="$t('ca3SApp.AccountStatus.' + aCMEAccount.status)">{{aCMEAccount.status}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.aCMEAccount.termsOfServiceAgreed')">Terms Of Service Agreed</span>
                    </dt>
                    <dd>
                        <span>{{aCMEAccount.termsOfServiceAgreed}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.aCMEAccount.publicKeyHash')">Public Key Hash</span>
                    </dt>
                    <dd>
                        <span>{{aCMEAccount.publicKeyHash}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.aCMEAccount.publicKey')">Public Key</span>
                    </dt>
                    <dd>
                        <span>{{aCMEAccount.publicKey}}</span>
                    </dd>
                </dl>
                <button type="submit"
                        v-on:click.prevent="previousState()"
                        class="btn btn-info">
                    <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                </button>
                <router-link v-if="aCMEAccount.id" :to="{name: 'ACMEAccountEdit', params: {aCMEAccountId: aCMEAccount.id}}" tag="button" class="btn btn-primary">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
                </router-link>
            </div>
        </div>
    </div>
</template>

<script lang="ts" src="./acme-account-details.component.ts">
</script>
