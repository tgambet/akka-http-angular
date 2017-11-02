import { Component, OnInit } from '@angular/core';
import { webSocket }        from 'rxjs/observable/dom/webSocket'
import { WebSocketSubject } from 'rxjs/observable/dom/WebSocketSubject'

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  title = 'app';

  subject: WebSocketSubject<string>;

  ngOnInit(): void {

    this.subject = webSocket('ws://localhost:8080/socket');

    let subscription =
      this.subject
        .subscribe(
          (msg) => console.log(msg),
          (err) => console.log(err),
          () => console.log('complete')
        );

    setTimeout(() => this.subject.next('test'), 1000)
    setTimeout(() => this.subject.next('test'), 1000)
    setTimeout(() => this.subject.next('test'), 1000)
    setTimeout(() => this.subject.next('test'), 1000)

  }



}
