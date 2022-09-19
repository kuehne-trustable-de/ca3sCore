<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="acmeContact">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.acmeContact.detail.title')">AcmeContact</span> {{acmeContact.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.acmeContact.contactId')">Contact Id</span>
                    </dt>
                    <dd>
                        <span>{{acmeContact.contactId}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.acmeContact.contactUrl')">Contact Url</span>
                    </dt>
                    <dd>
                        <span>{{acmeContact.contactUrl}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.acmeContact.account')">Account</span>
                    </dt>
                    <dd>
                        <div v-if="acmeContact.account">
                            <router-link :to="{name: 'AcmeAccountView', params: {aCMEAccountId: acmeContact.account.id}}">{{acmeContact.account.id}}</router-link>
                        </div>
                    </dd>
                </dl>
                <button type="submit"
                        v-on:click.prevent="previousState()"
                        class="btn btn-info">
                    <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                </button>
                <router-link v-if="acmeContact.id" :to="{name: 'AcmeContactEdit', params: {acmeContactId: acmeContact.id}}" tag="button" class="btn btn-primary">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
                </router-link>
            </div>
        </div>
    </div>
</template>

<script lang="ts" src="./acme-contact-details.component.ts">
</script>
