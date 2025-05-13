import axios, { AxiosPromise } from 'axios';

export default class MetricsService {
  public getMetrics(): AxiosPromise<any> {
    return axios.get('actuator/jhimetrics');
  }

  public retrieveThreadDump(): AxiosPromise<any> {
    return axios.get('actuator/threaddump');
  }
}
