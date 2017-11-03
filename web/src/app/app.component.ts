import { Component, OnInit, ViewChild }    from '@angular/core';
import { webSocket }                       from 'rxjs/observable/dom/webSocket'
import { WebSocketSubject }                from 'rxjs/observable/dom/WebSocketSubject'
import * as Material                       from '@angular/material';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  title = 'app';

  @ViewChild("sidenav")
  sidenav: Material.MatSidenav;

  sidenavMode: String = "over";

  subject: WebSocketSubject<string>;

  constructor(breakpointObserver: BreakpointObserver) {
    breakpointObserver.observe([
      Breakpoints.Web
    ]).subscribe(result => {
      if (!result.matches) {
        this.sidenavMode = "over";
        this.sidenav && this.sidenav.close();
      } else {
        this.sidenavMode = "side";
        this.sidenav && this.sidenav.open();
      }
    });
  }

  ngOnInit(): void {


    // this.subject = webSocket('ws://localhost:8080/socket');
    //
    // let subscription =
    //   this.subject
    //     .subscribe(
    //       (msg) => console.log(msg),
    //       (err) => console.log(err),
    //       () => console.log('complete')
    //     );
    //
    // setTimeout(() => this.subject.next('test'), 1000)
    // setTimeout(() => this.subject.next('test'), 1000)
    // setTimeout(() => this.subject.next('test'), 1000)
    // setTimeout(() => this.subject.next('test'), 1000)

  }



}
