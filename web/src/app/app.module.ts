import { NgModule }                from '@angular/core';
import { BrowserModule }           from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import * as Material               from '@angular/material';
import { BreakpointObserver, MediaMatcher } from '@angular/cdk/layout';

import { AppComponent } from './app.component';

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    Material.MatMenuModule,
    Material.MatSidenavModule,
    Material.MatToolbarModule,
    Material.MatIconModule,
    Material.MatInputModule,
    Material.MatFormFieldModule,
    Material.MatButtonModule,
    Material.MatCardModule
  ],
  providers: [
    MediaMatcher,
    BreakpointObserver
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
