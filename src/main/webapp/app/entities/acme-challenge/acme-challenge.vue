<template>
    <div>
        <h2 id="page-heading">
            <span v-text="$t('ca3SApp.acmeChallenge.home.title')" id="acme-challenge-heading">Acme Challenges</span>
            <router-link :to="{name: 'AcmeChallengeCreate'}" tag="button" id="jh-create-entity" class="btn btn-primary float-right jh-create-entity create-acme-challenge">
                <font-awesome-icon icon="plus"></font-awesome-icon>
                <span  v-text="$t('ca3SApp.acmeChallenge.home.createLabel')">
                    Create a new Acme Challenge
                </span>
            </router-link>
        </h2>
        <b-alert :show="dismissCountDown"
            dismissible
            :variant="alertType"
            @dismissed="dismissCountDown=0"
            @dismiss-count-down="countDownChanged">
            {{alertMessage}}
        </b-alert>
        <br/>
        <div class="alert alert-warning" v-if="!isFetching && acmeChallenges && acmeChallenges.length === 0">
            <span v-text="$t('ca3SApp.acmeChallenge.home.notFound')">No acmeChallenges found</span>
        </div>
        <div class="table-responsive" v-if="acmeChallenges && acmeChallenges.length > 0">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th><span v-text="$t('global.field.id')">ID</span></th>
                    <th><span v-text="$t('ca3SApp.acmeChallenge.challengeId')">Challenge Id</span></th>
                    <th><span v-text="$t('ca3SApp.acmeChallenge.type')">Type</span></th>
                    <th><span v-text="$t('ca3SApp.acmeChallenge.value')">Value</span></th>
                    <th><span v-text="$t('ca3SApp.acmeChallenge.token')">Token</span></th>
                    <th><span v-text="$t('ca3SApp.acmeChallenge.validated')">Validated</span></th>
                    <th><span v-text="$t('ca3SApp.acmeChallenge.status')">Status</span></th>
                    <th><span v-text="$t('ca3SApp.acmeChallenge.acmeAuthorization')">Acme Authorization</span></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="acmeChallenge in acmeChallenges"
                    :key="acmeChallenge.id">
                    <td>
                        <router-link :to="{name: 'AcmeChallengeView', params: {acmeChallengeId: acmeChallenge.id}}">{{acmeChallenge.id}}</router-link>
                    </td>
                    <td>{{acmeChallenge.challengeId}}</td>
                    <td>{{acmeChallenge.type}}</td>
                    <td>{{acmeChallenge.value}}</td>
                    <td>{{acmeChallenge.token}}</td>
                    <td>{{acmeChallenge.validated}}</td>
                    <td v-text="$t('ca3SApp.ChallengeStatus.' + acmeChallenge.status)">{{acmeChallenge.status}}</td>
                    <td>
                        <div v-if="acmeChallenge.acmeAuthorization">
                            <router-link :to="{name: 'AcmeAuthorizationView', params: {acmeAuthorizationId: acmeChallenge.acmeAuthorization.id}}">{{acmeChallenge.acmeAuthorization.id}}</router-link>
                        </div>
                    </td>
                    <td class="text-right">
                        <div class="btn-group">
                            <router-link :to="{name: 'AcmeChallengeView', params: {acmeChallengeId: acmeChallenge.id}}" tag="button" class="btn btn-info btn-sm details">
                                <font-awesome-icon icon="eye"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                            </router-link>
                            <router-link :to="{name: 'AcmeChallengeEdit', params: {acmeChallengeId: acmeChallenge.id}}"  tag="button" class="btn btn-primary btn-sm edit">
                                <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                            </router-link>
                            <b-button v-on:click="prepareRemove(acmeChallenge)"
                                   variant="danger"
                                   class="btn btn-sm"
                                   v-b-modal.removeEntity>
                                <font-awesome-icon icon="times"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.delete')">Delete</span>
                            </b-button>
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
        <b-modal ref="removeEntity" id="removeEntity" >
            <span slot="modal-title"><span id="ca3SApp.acmeChallenge.delete.question" v-text="$t('entity.delete.title')">Confirm delete operation</span></span>
            <div class="modal-body">
                <p id="jhi-delete-acmeChallenge-heading" v-bind:title="$t('ca3SApp.acmeChallenge.delete.question')">Are you sure you want to delete this Acme Challenge?</p>
            </div>
            <div slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
                <button type="button" class="btn btn-primary" id="jhi-confirm-delete-acmeChallenge" v-text="$t('entity.action.delete')" v-on:click="removeAcmeChallenge()">Delete</button>
            </div>
        </b-modal>
    </div>
</template>

<script lang="ts" src="./acme-challenge.component.ts">
</script>
