<template>

    <router-link :to="{ name: 'HelpTarget', params: {hash: targetHash, showNavBar: 'false'}}" >
        <font-awesome-icon icon="question-circle"></font-awesome-icon>
    </router-link>

</template>

<script lang="ts">

import {Component, Vue, Prop} from 'vue-property-decorator';

@Component
export default class HelpTag extends Vue{

    @Prop({required: true})
    public target: string = 'help';

    public targetHash: string = '#' + this.target;

    public mounted(): void {
        this.targetHash = '#' + this.target;
        console.log('targeting focus on  : ' + this.targetHash);
    }
}
</script>

<style></style>
