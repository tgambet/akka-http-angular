import {Injectable, OnDestroy}       from '@angular/core';
import {HttpClient}       from '@angular/common/http'
import {environment}      from "../../environments/environment";

import * as Rx from 'rxjs'

@Injectable()
export class HttpSocketClientService implements OnDestroy {

  constructor(private httpClient: HttpClient) { }

  private socketSubject: Rx.Subject<string>;

  private socketSubscription: Rx.Subscription;

  private id: number = 0;

  private static getSocketUrl() {
    let socketUrl: string = "";
    socketUrl += window.location.protocol == "http:" ? "ws://" : "wss://";
    socketUrl += window.location.hostname;
    if (environment.production) {
      if (window.location.port)
        socketUrl += ":" + window.location.port
    } else {
      socketUrl += ":" + environment.httpPort
    }
    socketUrl += "/socket";
    return socketUrl
  }

  private static getAPIUrl(path: string) {
    let url: string = "";
    url += window.location.protocol + "//" + window.location.hostname;
    if (environment.production) {
      if (window.location.port)
        url += ":" + window.location.port
    } else {
      url += ":" + environment.httpPort
    }
    url += path;
    return url
  }

  getSocket(): Rx.Subject<string> {
    if (!this.socketSubject) {
      this.socketSubject = Rx.Observable.webSocket(HttpSocketClientService.getSocketUrl());
      this.socketSubscription = this.socketSubject
        .subscribe(
          (msg) => {},
          (err) => { console.log(err); this.completeSocket() },
          () => this.completeSocket()
        );
    }
    return this.socketSubject;
  }

  send(message: any): void {
    this.getSocket().next(JSON.stringify(message));
  }

  private completeSocket() {
    if (this.socketSubscription)
      this.socketSubscription.unsubscribe();
    if (this.socketSubject)
      this.socketSubject.unsubscribe();
    this.socketSubject = null;
  }

  ngOnDestroy(): void {
    this.completeSocket();
  }

  get(path: string): Rx.Observable<Object> {

    if (!this.socketSubject) {
      return this.httpClient
        .get(HttpSocketClientService.getAPIUrl(path))
    } else {
      let requestId = this.id++
      let request = {
        method: "HttpRequest",
        entity: {
          method: "GET",
          url: HttpSocketClientService.getAPIUrl(path)
        },
        id: requestId
      };
      let expectResponse = this.getSocket().filter(s => s["id"] == requestId).take(1);
      let sendRequest = Rx.Observable.create(observer => {
        this.send(request);
        observer.complete();
        return () => {}
      });

      return sendRequest.concat(expectResponse)

    }

  }



  // postRequest(message: any) {
  //
  //   let request = {
  //     method: "HttpRequest",
  //     entity: {
  //       method: "POST",
  //       url: AppComponent.getAPIUrl("/api/post"),
  //       entity: message
  //     },
  //     id: this.id++
  //   };
  //
  //   this.httpClient
  //     .post(AppComponent.getAPIUrl("/api/post"), request)
  //     .subscribe(data =>
  //       this.logs.push(JSON.stringify(data))
  //     );
  //
  //   this.sendMessage(request)
  // }

}
