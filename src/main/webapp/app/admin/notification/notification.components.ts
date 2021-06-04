import { Component, Vue } from 'vue-property-decorator';
import axios from 'axios';

import { IProblemDetail } from '@/shared/model/transfer-object.model';

@Component
export default class Notification extends Vue {
  public certificateId: string = '1';
  public csrId: string = '1';

  public problemDetail: IProblemDetail = {};

  public mounted(): void {}

  public sendUserCertificateIssued(): void {
    window.console.info('calling sendUserCertificateIssued');
    this.problemDetail = {};
    const self = this;

    axios({
      method: 'post',
      url: '/api/notification/sendUserCertificateIssued/' + this.certificateId,
      responseType: 'stream'
    })
      .then(function(response) {
        window.console.info('api/notification/sendUserCertificateIssued returns ' + response.data);
        if (response.data.title !== undefined) {
          self.problemDetail = response.data;
        }
      })
      .catch(function(error) {
        window.console.info('api/notification/sendUserCertificateIssued returns ' + error);
        if (error.response.data.title !== undefined) {
          self.problemDetail = error.response.data;
        }
      });
  }

  public sendUserCertificateRejected(): void {
    window.console.info('calling sendUserCertificateRejected');
    this.problemDetail = {};
    const self = this;

    axios({
      method: 'post',
      url: '/api/notification/sendUserCertificateRejected/' + this.csrId,
      responseType: 'stream'
    })
      .then(function(response) {
        window.console.info('api/notification/sendUserCertificateRejected returns ' + response.data);
        if (response.data.title !== undefined) {
          self.problemDetail = response.data;
        }
      })
      .catch(function(error) {
        if (error.response.data.title !== undefined) {
          self.problemDetail = error.response.data;
        }
      });
  }

  public sendExpiryPendingSummary(): void {
    window.console.info('calling sendExpiryPendingInfo');
    this.problemDetail = {};
    const self = this;

    axios({
      method: 'post',
      url: '/api/notification/sendExpiryPendingSummary',
      responseType: 'stream'
    })
      .then(function(response) {
        window.console.info('api/notification/sendExpiryPendingSummary returns ' + response.data);
        if (response.data.title !== undefined) {
          self.problemDetail = response.data;
        }
      })
      .catch(function(error) {
        if (error.response.data.title !== undefined) {
          self.problemDetail = error.response.data;
        }
      });
  }
}
