<template>
    <div class="row justify-content-center">
        <div class="col-8">
            <div v-if="requestAttributeValue">
                <h2 class="jh-entity-heading"><span v-text="$t('ca3SApp.requestAttributeValue.detail.title')">RequestAttributeValue</span> {{requestAttributeValue.id}}</h2>
                <dl class="row jh-entity-details">
                    <dt>
                        <span v-text="$t('ca3SApp.requestAttributeValue.attributeValue')">Attribute Value</span>
                    </dt>
                    <dd>
                        <span>{{requestAttributeValue.attributeValue}}</span>
                    </dd>
                    <dt>
                        <span v-text="$t('ca3SApp.requestAttributeValue.reqAttr')">Req Attr</span>
                    </dt>
                    <dd>
                        <div v-if="requestAttributeValue.reqAttr">
                            <router-link :to="{name: 'RequestAttributeView', params: {requestAttributeId: requestAttributeValue.reqAttr.id}}">{{requestAttributeValue.reqAttr.id}}</router-link>
                        </div>
                    </dd>
                </dl>
                <button type="submit"
                        v-on:click.prevent="previousState()"
                        class="btn btn-info">
                    <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.back')"> Back</span>
                </button>
                <router-link v-if="requestAttributeValue.id" :to="{name: 'RequestAttributeValueEdit', params: {requestAttributeValueId: requestAttributeValue.id}}" tag="button" class="btn btn-primary">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="$t('entity.action.edit')"> Edit</span>
                </router-link>
            </div>
        </div>
    </div>
</template>

<script lang="ts" src="./request-attribute-value-details.component.ts">
</script>
