<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="rDN">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.rDN.detail.title')">RDN</span> {{rDN.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.rDN.csr')">Csr</span>
                    </dt>
                    <dd>
                        <div v-if="rDN.csr">
                            <router-link :to="{name: 'CSRView', params: {cSRId: rDN.csr.id}}">{{rDN.csr.id}}</router-link>
                        </div>
                    </dd>
                </dl>
                <button type="submit"
                        v-on:click.prevent="previousState()"
                        class="btn btn-info">
                    <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                </button>
                <router-link v-if="rDN.id" :to="{name: 'RDNEdit', params: {rDNId: rDN.id}}" tag="button" class="btn btn-primary">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
                </router-link>
            </div>
        </div>
    </div>
</template>

<script lang="ts" src="./rdn-details.component.ts">
</script>
