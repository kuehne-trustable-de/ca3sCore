import * as SockJS from 'sockjs-client';
import * as Stomp from 'webstomp-client';
import { Observable, Observer } from 'rxjs';
import VueRouter from 'vue-router';

export default class TrackerService {
  public stompClient: Stomp.Client = null;
  public subscriber: any = null;
  public connection: Promise<any>;
  public connectedPromise: any = null;
  public listener: Observable<any>;
  public listenerObserver: Observer<any> = null;
  public alreadyConnectedOnce = false;

  private router: VueRouter;

  constructor(router: VueRouter) {
    this.router = router;
    this.connection = this.createConnection();
    this.listener = this.createListener();
  }

  public connect(): void {
    // tracker disabled
    /*
    if (this.connectedPromise === null) {
      this.connection = this.createConnection();
    }
    // building absolute path so that websocket doesn't fail when deploying with a context path
    const loc = window.location;
    const baseHref = document.querySelector('base').getAttribute('href');
    let url;
    url = '//' + loc.host + baseHref + 'websocket/tracker';
    const authToken = localStorage.getItem('jhi-authenticationToken') || sessionStorage.getItem('jhi-authenticationToken');
    if (authToken) {
      url += '?access_token=' + authToken;
    }
    const socket = new SockJS(url);
    this.stompClient = Stomp.over(socket, { protocols: ['v12.stomp'] });
    const headers = {};
    this.stompClient.connect(headers, () => this.afterConnect());

 */
  }

  public afterConnect() {
    /*
    this.connectedPromise('success');
    this.connectedPromise = null;
    this.sendActivity();
    if (!this.alreadyConnectedOnce) {
      this.router.afterEach(() => this.sendActivity());
      this.alreadyConnectedOnce = true;
    }

     */
  }

  public disconnect(): void {
    if (this.stompClient !== null) {
      this.stompClient.disconnect();
      this.stompClient = null;
    }
    this.router.afterEach(() => {});
    this.alreadyConnectedOnce = false;
  }

  public receive(): Observable<any> {
    return this.listener;
  }

  public sendActivity(): void {
    /*
    if (this.stompClient !== null && this.stompClient.connected) {
      this.stompClient.send(
        '/topic/activity', // destination
        JSON.stringify({ page: this.router.currentRoute.fullPath }), // body
        {} // header
      );
    }

     */
  }

  public subscribe(): void {
    /*
    this.connection.then(() => {
      this.subscriber = this.stompClient.subscribe('/topic/tracker', data => {
        this.listenerObserver.next(JSON.parse(data.body));
      });
    });

     */
  }

  public unsubscribe(): any {
    if (this.subscriber !== null) {
      this.subscriber.unsubscribe();
    }
    this.listener = this.createListener();
  }

  public createListener(): any {
    return new Observable(observer => {
      this.listenerObserver = observer;
    });
  }

  public createConnection(): Promise<any> {
    return new Promise((resolve, reject) => (this.connectedPromise = resolve));
  }
}
