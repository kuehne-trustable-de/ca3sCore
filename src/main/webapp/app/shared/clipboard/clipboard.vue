<template>

    <b-button v-on:click="copyToClipboard()" class="btn btn-sm copy copy-button" >
        <font-awesome-icon icon="clipboard"></font-awesome-icon>
        <span class="d-none d-md-inline" v-text="$t('entity.action.copy')">Copy</span>
    </b-button>

</template>

<script lang="ts">

import {Component, Vue, Prop} from 'vue-property-decorator';

@Component
export default class CopyClipboardButton extends Vue {

    @Prop({required: true})
    public contentElementId: string;

    public copyToClipboard() {
        /* Get the text field */
        const copyText = document.getElementById(this.contentElementId) as HTMLInputElement;

        /* Select the text field */
        copyText.select();
        copyText.setSelectionRange(0, 99999); /* For mobile devices */

        /* Copy the text inside the text field */
        document.execCommand('copy');
    }
}
</script>

<style scoped>

.copy-button {
    position: absolute;
    right: 3px;
    top: 3px;
    outline: none;
    cursor: pointer;
    font-size: 11px;
    padding: 3px 6px;
}
</style>
