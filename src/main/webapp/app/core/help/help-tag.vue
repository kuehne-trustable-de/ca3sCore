<template>

    <router-link :to="{ name: 'HelpTarget' + role, params: {hash: '#' + target, showNavBar: 'false', lang: lang}}" >
        &nbsp;<font-awesome-icon icon="question-circle"></font-awesome-icon>&nbsp;
    </router-link>

</template>

<script lang="ts">

import {Component, Vue, Prop} from 'vue-property-decorator';

@Component
export default class HelpTag extends Vue{

    @Prop({required: true})
    public target: string;

    @Prop({required: false, default: ''})
    public role: string;

//    public targetName: string = 'HelpTarget' + this.role;

//    public targetHash: string = '#' + this.target;

    public lang: string = this.$store.getters.currentLanguage;

    public mounted(): void {
        console.log('targeting focus on  : ' + this.target + ', lang : ' + this.$store.getters.currentLanguage);
    }
}
</script>

<style></style>
