import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import * as Material                             from '@angular/material';
import {BreakpointObserver}                      from '@angular/cdk/layout';
import {Subscription}                            from 'rxjs/Subscription'

import {HttpSocketClientService}                 from "./services/http-socket-client.service";

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
    this.socket && this.socket.unsubscribe()
  }

  openSocket() {
    this.socket = this.httpSocketClient.getSocket().subscribe(
      (msg)=> this.logs.push("socket: " + JSON.stringify(msg)),
      error => this.logs.push("socket error: " + JSON.stringify(error)),
      () => this.logs.push("socket closed")
    );
  }

  closeSocket() {
    this.httpSocketClient.closeSocket();
  }

  sendMessage(message: any) {
    this.httpSocketClient.send({message: message});
  }

  getRequest() {
    this.httpSocketClient.get("/api/get")
      .subscribe(
        data => this.logs.push("GET: " + JSON.stringify(data)),
        error => console.log(error),
        () => {}
      )
  }

  postRequest(entity: Object) {
    this.httpSocketClient.post("/api/post", entity)
      .subscribe(data => {
          this.logs.push("POST: " + JSON.stringify(data))
        }
      )
  }

}
