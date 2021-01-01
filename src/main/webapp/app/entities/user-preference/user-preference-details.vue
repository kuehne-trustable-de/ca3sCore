<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="userPreference">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.userPreference.detail.title')">UserPreference</span> {{userPreference.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.userPreference.userId')">User Id</span>
                    </dt>
                    <dd>
                        <span>{{userPreference.userId}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.userPreference.name')">Name</span>
                    </dt>
                    <dd>
                        <span>{{userPreference.name}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.userPreference.content')">Content</span>
                    </dt>
                    <dd>
                        <span>{{userPreference.content}}</span>
                    </dd>
                </dl>
                <button type="submit"
                        v-on:click.prevent="previousState()"
                        class="btn btn-info">
                    <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                </button>
                <router-link v-if="userPreference.id" :to="{name: 'UserPreferenceEdit', params: {userPreferenceId: userPreference.id}}" tag="button" class="btn btn-primary">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
                </router-link>
            </div>
        </div>
    </div>
</template>

<script lang="ts" src="./user-preference-details.component.ts">
</script>
