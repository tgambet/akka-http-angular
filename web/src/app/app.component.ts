import { Component, OnInit, ViewChild, OnDestroy }    from '@angular/core';
import { webSocket }                       from 'rxjs/observable/dom/webSocket'
import { WebSocketSubject }                from 'rxjs/observable/dom/WebSocketSubject'
import { Subscription }                    from 'rxjs/Subscription'
import * as Material                       from '@angular/material';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';

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

  constructor(private breakpointObserver: BreakpointObserver) {}

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
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  openSocket() {
    if (!this.subject) {
      this.subject = webSocket('ws://localhost:8080/socket');
      if (this.subscription) {
        this.subscription.unsubscribe();
      }
      this.subscription =
        this.subject
          .subscribe(
            (msg) => this.logs.push(JSON.stringify(msg)),
            (err) => this.subject = null,
            () => this.subject = null
          );
    }
  }

  sendMessage(message: string) {
    if (this.subject)
      this.subject.next(message);
  }

}
