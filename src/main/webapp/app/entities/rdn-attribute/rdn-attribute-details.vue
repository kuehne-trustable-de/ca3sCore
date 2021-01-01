<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="rDNAttribute">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.rDNAttribute.detail.title')">RDNAttribute</span> {{rDNAttribute.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.rDNAttribute.attributeType')">Attribute Type</span>
                    </dt>
                    <dd>
                        <span>{{rDNAttribute.attributeType}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.rDNAttribute.attributeValue')">Attribute Value</span>
                    </dt>
                    <dd>
                        <span>{{rDNAttribute.attributeValue}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.rDNAttribute.rdn')">Rdn</span>
                    </dt>
                    <dd>
                        <div v-if="rDNAttribute.rdn">
                            <router-link :to="{name: 'RDNView', params: {rDNId: rDNAttribute.rdn.id}}">{{rDNAttribute.rdn.id}}</router-link>
                        </div>
                    </dd>
                </dl>
                <button type="submit"
                        v-on:click.prevent="previousState()"
                        class="btn btn-info">
                    <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                </button>
                <router-link v-if="rDNAttribute.id" :to="{name: 'RDNAttributeEdit', params: {rDNAttributeId: rDNAttribute.id}}" tag="button" class="btn btn-primary">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
                </router-link>
            </div>
        </div>
    </div>
</template>

<script lang="ts" src="./rdn-attribute-details.component.ts">
</script>
