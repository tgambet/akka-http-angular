import { NgModule }                from '@angular/core';
import { BrowserModule }           from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import * as Material               from '@angular/material';

import { AppComponent } from './app.component';

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    Material.MatMenu,
    Material.MatSidenav,
    Material.MatToolbar
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
