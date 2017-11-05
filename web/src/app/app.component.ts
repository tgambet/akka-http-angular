import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {HttpClient}                              from '@angular/common/http'
import * as Material                             from '@angular/material';
import {BreakpointObserver}                      from '@angular/cdk/layout';
import {webSocket}                               from 'rxjs/observable/dom/webSocket'
import {WebSocketSubject}                        from 'rxjs/observable/dom/WebSocketSubject'
import {Subscription}                            from 'rxjs/Subscription'

import {HttpSocketClientService}                 from "./services/http-socket-client.service";
import {environment}                             from "../environments/environment";

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

  socket: Subscription;

  logs: Array<string> = [];

  constructor(
    private breakpointObserver: BreakpointObserver,
    private httpClient: HttpClient,
    private httpSocketClient: HttpSocketClientService
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
    this.socket.unsubscribe()
  }



  openSocket() {
    this.socket = this.httpSocketClient.getSocket().subscribe(
      (msg)=> this.logs.push("socket: " + JSON.stringify(msg))
    );
  }

  sendMessage(message: any) {
    this.httpSocketClient.send(JSON.stringify(message));
  }

  getRequest() {

    this.httpSocketClient.get("/ap/get")
      .subscribe(data => {
          console.log(data)
          this.logs.push("GET: " + JSON.stringify(data))
        }
      )

    // this.httpClient
    //   .get(AppComponent.getAPIUrl("/api/get"))
    //   .subscribe(data =>
    //     this.logs.push(JSON.stringify(data))
    //   );
    //
    // let request = {
    //   method: "HttpRequest",
    //   entity: {
    //     method: "GET",
    //     url: AppComponent.getAPIUrl("/api/get")
    //   },
    //   id: this.id++
    // };
    //
    // this.sendMessage(request)
  }

  // id: number = 0;
  //
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
