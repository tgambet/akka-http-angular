import {Injectable, OnDestroy}       from '@angular/core';
import {HttpClient, HttpErrorResponse}       from '@angular/common/http'
import {environment}      from "../../environments/environment";

import * as Rx from 'rxjs'

@Injectable()
export class HttpSocketClientService implements OnDestroy {

  constructor(private httpClient: HttpClient) { }

  private socket: Rx.Subject<string>;

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

  getSocket(): Rx.Subject<Object> {
    if (!this.socket) {
      this.socket = Rx.Observable.webSocket(HttpSocketClientService.getSocketUrl());
      this.socket.subscribe(
        (next)=> {},
        (error)=> this.closeSocket(),
        () => this.closeSocket()
      )
    }
    return this.socket;
  }

  closeSocket(): void {
    this.socket && this.socket.unsubscribe();
    this.socket = null;
  }

  isSocketOpen(): boolean {
    return this.socket != null;
  }

  send(message: any): void {
    this.getSocket().next(JSON.stringify(message));
  }

  ngOnDestroy(): void {
    this.closeSocket();
  }

  get(path: string): Rx.Observable<Object> {
    if (!this.socket) {
      return this.httpClient.get(HttpSocketClientService.getAPIUrl(path))
    } else {
      let request: HttpRequest = {
        method: "HttpRequest",
        entity: {
          method: "GET",
          url: HttpSocketClientService.getAPIUrl(path)
        },
        id: this.id++
      };
      return this.sendRequest(request)
    }
  }

  post(path: string, entity: Object): Rx.Observable<Object> {
    if (!this.socket) {
      return this.httpClient.post(HttpSocketClientService.getAPIUrl(path), entity)
    } else {
      let request: HttpRequest = {
        method: "HttpRequest",
        entity: {
          method: "POST",
          url: HttpSocketClientService.getAPIUrl(path),
          entity: entity
        },
        id: this.id++
      };
      return this.sendRequest(request)
    }
  }

  private sendRequest(request: HttpRequest): Rx.Observable<Object> {
    let expectResponse =
      this.getSocket()
        .filter((r: HttpResponse) => r.method == "HttpResponse" && r.id == request.id)
        .map((r: HttpResponse) => {
          let status = r.entity.status;
          let statusText = r.entity.statusText;
          let entity = r.entity.entity;
          if (status >= 400)
            throw new HttpErrorResponse({ error: entity, status: status, statusText: statusText, url: request.entity.url});
          return entity;
        })
        .take(1);
    let sendRequest = Rx.Observable.create(observer => {
      this.send(request);
      observer.complete();
      return () => {}
    });
    return sendRequest.concat(expectResponse)
  }

}

export interface HttpRequest {
  method: string,
  id: number,
  entity: {
    method: string,
    url: string,
    entity?: Object
  }
}

export interface HttpResponse {
  method: string,
  id: number,
  entity: {
    status: number,
    statusText: string,
    entity: Object
  }
}
