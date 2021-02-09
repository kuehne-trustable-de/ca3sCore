<template>

    <router-link :to="{ name: 'HelpTarget', params: {hash: targetHash, showNavBar: 'false', lang: lang}}" >
        <font-awesome-icon icon="question-circle"></font-awesome-icon>
    </router-link>

</template>

<script lang="ts">

import {Component, Vue, Prop} from 'vue-property-decorator';

@Component
export default class HelpTag extends Vue{

    @Prop({required: true})
    public target: string;

    public targetHash: string = '#' + this.target;

    public lang: string = this.$store.getters.currentLanguage;

    public mounted(): void {
        this.targetHash = '#' + this.target;
        console.log('targeting focus on  : ' + this.targetHash + ', lang : ' + this.$store.getters.currentLanguage);
    }
}
</script>

<style></style>
