import { Component, OnInit, ViewChild, OnDestroy }    from '@angular/core';
import { HttpClient, HttpRequest }                      from '@angular/common/http'
import { webSocket }                       from 'rxjs/observable/dom/webSocket'
import { WebSocketSubject }                from 'rxjs/observable/dom/WebSocketSubject'
import { Subscription }                    from 'rxjs/Subscription'
import * as Material                       from '@angular/material';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';

import { environment } from "../environments/environment";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit, OnDestroy {

  title = 'app';

  @ViewChild("sidenav")
  sidenav: Material.MatSidenav;

  isSmallScreen: boolean;

  subject: WebSocketSubject<string>;

  subscription: Subscription;

  logs: Array<string> = []

  constructor(
    private breakpointObserver: BreakpointObserver,
    private httpClient: HttpClient
  ) {}

  ngOnInit(): void {

    this.breakpointObserver.observe('(max-width: 960px)').subscribe(result => {
      if (result.matches) {
        this.isSmallScreen = true;
        this.sidenav.close();
      } else {
        this.isSmallScreen = false;
        this.sidenav.open();
      }
    });

  }

  ngOnDestroy(): void {
    if (this.subscription)
      this.subscription.unsubscribe();
    if (this.subject)
      this.subject.unsubscribe()
  }

  static getSocketUrl() {
    let socketUrl: string = "";
    socketUrl += window.location.protocol == "http:" ? "ws://" : "wss://";
    socketUrl += window.location.hostname;
    if (environment.production) {
      socketUrl += ":" + window.location.port
    } else {
      socketUrl += ":8080"
    }
    socketUrl += "/socket";
    return socketUrl
  }

  openSocket() {
    if (!this.subject) {
      this.subject = webSocket(AppComponent.getSocketUrl());
      this.subscription =
        this.subject
          .subscribe(
            (msg) => this.logs.push(JSON.stringify(msg)),
            (err) => { console.log(err); this.completeSocket() },
            () => this.completeSocket()
          );
    }
  }

  sendMessage(message: string) {
    if (this.subject)
      this.subject.next(message);
    else {
      this.openSocket();
      this.subject.next(message);
    }
  }

  completeSocket() {
    if (this.subscription)
      this.subscription.unsubscribe();
    if (this.subject)
      this.subject.unsubscribe();
    this.subscription = null;
    this.subject = null;
  }

  getRequest() {
    this.httpClient
      .get("http://localhost:8080/api/get")
      .subscribe(data =>
        this.logs.push(JSON.stringify(data))
      );

    let httpRequest: HttpRequest<null> = new HttpRequest("GET", "http://localhost:8080/api/get");

    let request = {
      method: "",
      message: ""
    };

    this.subject.next(JSON.stringify(request))
  }

  postRequest(message: string) {
    let testObject = {
      method: "POST",
      body: message
    };

    this.httpClient
      .post("http://localhost:8080/api/post", testObject)
      .subscribe(data =>
        this.logs.push(JSON.stringify(data))
      )
  }

}
