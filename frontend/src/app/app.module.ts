import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { FooterComponent } from './component/footer/footer.component';
import { NavbarComponent } from './component/navbar/navbar.component';
import { SignupComponent } from './component/signUp/signup.component';
import { LoginComponent } from './component/login/login.component';
import { LandingComponent } from './component/landing/landing.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NgOptimizedImage } from '@angular/common';
import {
  provideHttpClient,
  withInterceptorsFromDi,
} from '@angular/common/http';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { PostComponent } from './component/post/post.component';
import { ModifyPostComponent } from './component/modifyPost/modifyPost.component';
import { CommunityComponent } from './component/community/community.component';
import { ContenteditableValueAccessor } from './component/modifyPost/contenteditable-value-accessor';
import { ModifyCommunityComponent } from './component/modifyCommunity/modifyCommunity.component';
import { NewCommunityComponent } from './component/newCommunity/newCommunity.component';
import { NewPostComponent } from './component/newPost/newPost.component';

@NgModule({
  declarations: [
    AppComponent,
    FooterComponent,
    NavbarComponent,
    SignupComponent,
    LoginComponent,
    LandingComponent,
    PostComponent,
    ContenteditableValueAccessor,
    ModifyPostComponent,
    CommunityComponent,
    ModifyCommunityComponent,
    NewCommunityComponent,
    NewPostComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    NgOptimizedImage,
    ReactiveFormsModule,
    BrowserAnimationsModule,
  ],
  providers: [provideHttpClient(withInterceptorsFromDi())],
  bootstrap: [AppComponent],
})
export class AppModule {}
