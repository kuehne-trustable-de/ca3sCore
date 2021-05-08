<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="bPNMProcessInfo">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.bPNMProcessInfo.detail.title')">BPMN Info</span> {{bPNMProcessInfo.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.bPNMProcessInfo.name')">Name</span>
                    </dt>
                    <dd>
                        <span>{{bPNMProcessInfo.name}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.bPNMProcessInfo.version')">Version</span>
                    </dt>
                    <dd>
                        <span>{{bPNMProcessInfo.version}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.bPNMProcessInfo.type')">Type</span>
                    </dt>
                    <dd>
                        <span v-text="$t('ca3SApp.BPNMProcessType.' + bPNMProcessInfo.type)">{{bPNMProcessInfo.type}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.bPNMProcessInfo.author')">Author</span>
                    </dt>
                    <dd>
                        <span>{{bPNMProcessInfo.author}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.bPNMProcessInfo.lastChange')">Last Change</span>
                    </dt>
                    <dd>
                        <span v-if="bPNMProcessInfo.lastChange">{{$d(Date.parse(bPNMProcessInfo.lastChange), 'long') }}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.bPNMProcessInfo.signatureBase64')">Signature Base 64</span>
                    </dt>
                    <dd>
                        <span>{{bPNMProcessInfo.signatureBase64}}</span>
                    </dd>
                </dl>
            </div>

            <vue-bpmn
                url="/api/bpmn/CAInvocationProcess:2:bca343e4-7ace-11eb-b7db-0a0027000005"
                v-on:error="handleError"
                v-on:shown="handleShown"
                v-on:loading="handleLoading"
            ></vue-bpmn>

            <form name="editForm" role="form" novalidate>
                <div>


                    <button type="submit"
                            v-on:click.prevent="previousState()"
                            class="btn btn-info">
                        <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                    </button>

                </div>
            </form>

        </div>
    </div>
</template>

<script lang="ts" src="./bpmn-info.component.ts">
</script>
