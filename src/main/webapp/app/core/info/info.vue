<template>
    <div>
        <h1><b>C</b>ertificate <b>A</b><sup><small>1</small></sup>utomation, <b>A</b><sup><small>2</small></sup>uthorization and <b>A</b><sup><small>3</small></sup>dministration <b>S</b>ervice (ca<sup><small>3</small></sup>s)</h1>

        <h2>Overview</h2>

        <span>ca3s is a CA support system with a flexible RA part using BPMN aiming to automate as much as possible.
            Therefore providing ACME and SCEP interfaces in addition to the usual web form. Aggregating certificate sets
                from different sources and using CMP-connected CAs or ADCS instances for certificate creation.</span>

        <p/>
        <span>The feature list</span>

        <ul>

            <li>Manage all your CA instances (CMP and ADCS)</li>

            <li>Keep track of expiration of all your relevant certificates from all sources</li>

            <li>Analyze the key algorithms, key length, hash and padding algorithms in use</li>

            <li>Offer a convenient web interface for the requestors and the RA officers</li>

            <li>Use of the <a href="https://badkeys.info/">badkeys</a> project (if installed locally) to check keys for known weaknesses</li>

            <p/>
            And most important for a reliable PKI infrastructure:

            <li>Automate issuance and renewal as far as possible</li>

            <li>Use BPMN to define organization specific rules</li>

            <li>Offer well established interfaces (ACME and SCEP) for easy automation</li>

        </ul>

        The project is open sourced under <a href="https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12" target="_blank">EUPL</a> and can be found at <a href="https://github.com/kuehne-trustable-de/ca3sCore" target="_blank">github</a>.
        Please file bug reports, questions and feature proposals at github's <a href="https://github.com/kuehne-trustable-de/ca3sCore/issues" target="_blank">issue tracker</a>.
        For professional support please contact <a href="mailto:info@trustable.de">trustable's support</a>.
        <p/>

        <h2 v-if="info.git" id="configuration-page-heading" v-text="$t('ca3SApp.info.title')"></h2>
        <table v-if="info.git" class="table table-striped table-bordered table-responsive d-table">
            <thead>
            <tr>
                <th class="w-40"><span v-text="$t('ca3SApp.info.table.item')"></span></th>
                <th class="w-60"><span v-text="$t('ca3SApp.info.table.properties')"></span></th>
            </tr>
            </thead>
            <tbody>

            <tr>
                <td><span>Maven build version</span></td>
                <td>
                    <span class="float-right">{{ info.git.branch }}</span>
                </td>
            </tr>
            <tr>
                <td><span>Active Profile</span></td>
                <td>
                    <span class="float-right">{{ info.activeProfiles[0] }}</span>
                </td>
            </tr>
            <tr>
                <td><span>Git Commit</span></td>
                <td>
                    <span class="float-right">{{ info.git.commit.id }} from {{ info.git.commit.time }}</span>
                </td>
            </tr>
            </tbody>
        </table>

        <div v-if="isRAOrAdmin()">
            <form>
                <button type="button" id="retrieveCertificates" class="btn btn-primary"
                        v-on:click="postSchedule('retrieveCertificates')">
                    <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span
                    v-text="$t('ca3SApp.schedule.action.retrieveCertificates')"></span>
                </button>

                <button type="button" id="updateRevocationStatus" class="btn btn-primary"
                        v-on:click="postSchedule('updateRevocationStatus')">
                    <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span
                    v-text="$t('ca3SApp.schedule.action.updateRevocationStatus')"></span>
                </button>
            </form>
        </div>
    </div>
</template>

<script lang="ts" src="./info.components.ts">
</script>
