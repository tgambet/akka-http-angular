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

  isSmallScreen: boolean;

  subject: WebSocketSubject<string>;

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
