import Component from 'vue-class-component';
import { Inject, Vue } from 'vue-property-decorator';
import LoginService from '@/account/login.service';

import { ICertificate } from '@/shared/model/certificate.model';
import { colFieldToStr, formatUtcDate, makeQueryStringFromObj } from '@/shared/utils';

import { VuejsDatatableFactory, TColumnsDefinition, ITableContentParam, IDataFnParams } from 'vuejs-datatable';

import axios from 'axios';

// import VueAxios from 'vue-axios'
// Vue.use(VueAxios, axios)

Vue.use(VuejsDatatableFactory);


@Component
export default class Home extends Vue {
	@Inject('loginService')
	private loginService: () => LoginService;

	public openLogin(): void {
		this.loginService().openLogin((<any>this).$root);
	}

	public get authenticated(): boolean {
		return this.$store.getters.authenticated;
	}

	public get username(): string {
		return this.$store.getters.account ? this.$store.getters.account.login : '';
	}

	el() { return '#vue-certificates'; }
	data() {
		return {
			columns: [
				{ label: 'id', field: 'id' },
				{ label: 'subject', field: 'subject', headerClass: 'class-in-header second-class' },
				{ label: 'issuer', field: 'issuer' },
				{ label: 'type', field: 'type' },
				{ label: 'serial', field: 'serial' },
				{ label: 'validFrom', field: 'validFrom' },
				{ label: 'validTo', field: 'validTo' },
				{ label: 'revoked', field: 'revoked' },
				{ label: 'revokedSince', field: 'revokedSince' }
			] as TColumnsDefinition<ICertificate>,
			page: 1,
			filter: '',

			async getData({ sortBy, sortDir, perPage, page }: IDataFnParams<ICertificate>) {

				const sortParams = sortBy && sortDir ? {
					order: sortDir,
					sort: colFieldToStr(sortBy).replace(/\./g, '/'),
				} : {};

				const params = {
					// Sorting
					...sortParams,

					// Filtering
					// See https://documenter.getpostman.com/view/2025350/RWaEzAiG#json-field-masking
					filter: this.columns.map(col => colFieldToStr(col.field!).replace(/\./g, '/')).join(','),

					// Paging
					limit: perPage || 10,
					offset: ((page - 1) * perPage) || 0,
				};

				const baseApiUrl = 'api/certificates';
				const url = `${baseApiUrl}?${makeQueryStringFromObj(params)}`;

				const {
					// Data to display
					data,
					// Get the total number of matched items
					headers: { 'spacex-api-count': totalCount },
				} = await axios.get(url);

				return {
					rows: data,
					totalRowCount: totalCount,
				} as ITableContentParam<ICertificate>;
			},

		};
	}

}
