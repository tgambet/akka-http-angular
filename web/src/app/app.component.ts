import {Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import * as Material from '@angular/material';
import {BreakpointObserver} from '@angular/cdk/layout';
import {Subscription} from 'rxjs/Subscription';

import {HttpSocketClientService} from './services/http-socket-client.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit, OnDestroy {

  title = 'app';

  @ViewChild('sidenav')
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
    if (this.socket) {
      this.socket.unsubscribe();
    }
  }

  openSocket() {
    if (!this.socket) {
      this.socket = this.httpSocketClient.getSocket().subscribe(
        (msg) => this.logs.push('socket: ' + JSON.stringify(msg)),
        error => this.logs.push('socket error: ' + JSON.stringify(error)),
        () => this.logs.push('socket closed')
      );
    }
    return this.httpSocketClient.getSocket();
  }

  isSocketOpen() {
    return this.httpSocketClient.isSocketOpen();
  }

  closeSocket() {
    this.httpSocketClient.closeSocket();
    this.socket = null;
  }

  sendMessage(message: any) {
    this.openSocket().next(JSON.stringify({message: message}));
  }

  getRequest(endpoint: string) {
    this.httpSocketClient.get(endpoint)
      .subscribe(
        data => this.logs.push('GET: ' + JSON.stringify(data)),
        error => console.log(error),
        () => {}
      );
  }

  postRequest(endpoint: string, entity: Object) {
    this.httpSocketClient.post(endpoint, entity)
      .subscribe(data => {
          this.logs.push('POST: ' + JSON.stringify(data));
        }
      );
  }

}
