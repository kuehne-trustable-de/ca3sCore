<template>
    <div>
        <h2 id="page-heading">
            <span v-text="$t('ca3SApp.userPreference.home.title')" id="user-preference-heading">User Preferences</span>
            <router-link :to="{name: 'UserPreferenceCreate'}" tag="button" id="jh-create-entity" class="btn btn-primary float-right jh-create-entity create-user-preference">
                <font-awesome-icon icon="plus"></font-awesome-icon>
                <span  v-text="$t('ca3SApp.userPreference.home.createLabel')">
                    Create a new User Preference
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
        <div class="alert alert-warning" v-if="!isFetching && userPreferences && userPreferences.length === 0">
            <span v-text="$t('ca3SApp.userPreference.home.notFound')">No userPreferences found</span>
        </div>
        <div class="table-responsive" v-if="userPreferences && userPreferences.length > 0">
            <table class="table table-striped">
                <thead>
                <tr>
                    <th><span v-text="$t('global.field.id')">ID</span></th>
                    <th><span v-text="$t('ca3SApp.userPreference.userId')">User Id</span></th>
                    <th><span v-text="$t('ca3SApp.userPreference.name')">Name</span></th>
                    <th><span v-text="$t('ca3SApp.userPreference.content')">Content</span></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="userPreference in userPreferences"
                    :key="userPreference.id">
                    <td>
                        <router-link :to="{name: 'UserPreferenceView', params: {userPreferenceId: userPreference.id}}">{{userPreference.id}}</router-link>
                    </td>
                    <td>{{userPreference.userId}}</td>
                    <td>{{userPreference.name}}</td>
                    <td>{{userPreference.content}}</td>
                    <td class="text-right">
                        <div class="btn-group">
                            <router-link :to="{name: 'UserPreferenceView', params: {userPreferenceId: userPreference.id}}" tag="button" class="btn btn-info btn-sm details">
                                <font-awesome-icon icon="eye"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.view')">View</span>
                            </router-link>
                            <router-link :to="{name: 'UserPreferenceEdit', params: {userPreferenceId: userPreference.id}}"  tag="button" class="btn btn-primary btn-sm edit">
                                <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                                <span class="d-none d-md-inline" v-text="$t('entity.action.edit')">Edit</span>
                            </router-link>
                            <b-button v-on:click="prepareRemove(userPreference)"
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
            <span slot="modal-title"><span id="ca3SApp.userPreference.delete.question" v-text="$t('entity.delete.title')">Confirm delete operation</span></span>
            <div class="modal-body">
                <p id="jhi-delete-userPreference-heading" v-text="$t('ca3SApp.userPreference.delete.question', {'id': removeId})">Are you sure you want to delete this User Preference?</p>
            </div>
            <div slot="modal-footer">
                <button type="button" class="btn btn-secondary" v-text="$t('entity.action.cancel')" v-on:click="closeDialog()">Cancel</button>
                <button type="button" class="btn btn-primary" id="jhi-confirm-delete-userPreference" v-text="$t('entity.action.delete')" v-on:click="removeUserPreference()">Delete</button>
            </div>
        </b-modal>
    </div>
</template>

<script lang="ts" src="./user-preference.component.ts">
</script>
