<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="requestAttribute">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.requestAttribute.detail.title')">RequestAttribute</span> {{requestAttribute.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.requestAttribute.attributeType')">Attribute Type</span>
                    </dt>
                    <dd>
                        <span>{{requestAttribute.attributeType}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.requestAttribute.holdingRequestAttribute')">Holding Request Attribute</span>
                    </dt>
                    <dd>
                        <div v-if="requestAttribute.holdingRequestAttribute">
                            <router-link :to="{name: 'RequestAttributeValueView', params: {requestAttributeValueId: requestAttribute.holdingRequestAttribute.id}}">{{requestAttribute.holdingRequestAttribute.id}}</router-link>
                        </div>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.requestAttribute.csr')">Csr</span>
                    </dt>
                    <dd>
                        <div v-if="requestAttribute.csr">
                            <router-link :to="{name: 'CSRView', params: {cSRId: requestAttribute.csr.id}}">{{requestAttribute.csr.id}}</router-link>
                        </div>
                    </dd>
                </dl>
                <button type="submit"
                        v-on:click.prevent="previousState()"
                        class="btn btn-info">
                    <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                </button>
                <router-link v-if="requestAttribute.id" :to="{name: 'RequestAttributeEdit', params: {requestAttributeId: requestAttribute.id}}" tag="button" class="btn btn-primary">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
                </router-link>
            </div>
        </div>
    </div>
</template>

<script lang="ts" src="./request-attribute-details.component.ts">
</script>
