<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="acmeChallenge">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.acmeChallenge.detail.title')">AcmeChallenge</span> {{acmeChallenge.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.acmeChallenge.challengeId')">Challenge Id</span>
                    </dt>
                    <dd>
                        <span>{{acmeChallenge.challengeId}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.acmeChallenge.type')">Type</span>
                    </dt>
                    <dd>
                        <span>{{acmeChallenge.type}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.acmeChallenge.value')">Value</span>
                    </dt>
                    <dd>
                        <span>{{acmeChallenge.value}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.acmeChallenge.token')">Token</span>
                    </dt>
                    <dd>
                        <span>{{acmeChallenge.token}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.acmeChallenge.validated')">Validated</span>
                    </dt>
                    <dd>
                        <span v-if="acmeChallenge.validated">{{$d(Date.parse(acmeChallenge.validated), 'long') }}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.acmeChallenge.status')">Status</span>
                    </dt>
                    <dd>
                        <span v-text="$t('ca3SApp.ChallengeStatus.' + acmeChallenge.status)">{{acmeChallenge.status}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.acmeChallenge.acmeAuthorization')">Acme Authorization</span>
                    </dt>
                    <dd>
                        <div v-if="acmeChallenge.acmeAuthorization">
                            <router-link :to="{name: 'AcmeAuthorizationView', params: {acmeAuthorizationId: acmeChallenge.acmeAuthorization.id}}">{{acmeChallenge.acmeAuthorization.id}}</router-link>
                        </div>
                    </dd>
                </dl>
                <button type="submit"
                        v-on:click.prevent="previousState()"
                        class="btn btn-info">
                    <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                </button>
                <router-link v-if="acmeChallenge.id" :to="{name: 'AcmeChallengeEdit', params: {acmeChallengeId: acmeChallenge.id}}" tag="button" class="btn btn-primary">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
                </router-link>
            </div>
        </div>
    </div>
</template>

<script lang="ts" src="./acme-challenge-details.component.ts">
</script>
