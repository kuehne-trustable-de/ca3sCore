<template>
    <div>
        <h2 id="configuration-page-heading" v-text="$t('ca3SApp.info.title')">Info</h2>

        <form>
            <button type="button" id="retrieveCertificates" class="btn btn-primary" v-on:click="postSchedule('retrieveCertificates')">
                <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('ca3SApp.schedule.action.retrieveCertificates')">Retrieve Certificates</span>
            </button>

            <button type="button" id="updateRevocationStatus" class="btn btn-primary" v-on:click="postSchedule('updateRevocationStatus')">
                <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="$t('ca3SApp.schedule.action.updateRevocationStatus')">Update Revocation Status</span>
            </button>

        </form>

        <table v-if="info.git" class="table table-striped table-bordered table-responsive d-table">
            <thead>
            <tr>
                <th class="w-40" ><span v-text="$t('ca3SApp.info.table.item')">Item</span></th>
                <th class="w-60" ><span v-text="$t('ca3SApp.info.table.properties')">Value</span></th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td><span>Maven build version</span></td>
                <td>
                    <span class="float-right">{{info.git.build.version}} from {{info.git.build.time}}</span>
                </td>
            </tr>
            <tr>
                <td><span>Active Profile</span></td>
                <td>
                    <span class="float-right">{{info.activeProfiles[0]}}</span>
                </td>
            </tr>
            <tr>
                <td><span>Git Commit</span></td>
                <td>
                    <span class="float-right">{{info.git.commit.id.full}} from {{info.git.commit.time}}</span>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</template>

<script lang="ts" src="./info.components.ts">
</script>
