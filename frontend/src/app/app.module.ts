import { NgModule } from '@angular/core';
import { BrowserModule, Title } from '@angular/platform-browser';

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
import { UserComponent } from './component/user/user.component';
import { ModifyUserComponent } from './component/modifyUser/modifyUser.component';
import { SearchComponent } from './component/search/search.component';
import { ErrorComponent } from './component/error/error.component';
import { AdminComponent } from './component/admin/admin.component';
import { ChatComponent } from './component/chat/chat.component';

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
    UserComponent,
    ModifyUserComponent,
    SearchComponent,
    ErrorComponent,
    AdminComponent,
    ChatComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    NgOptimizedImage,
    ReactiveFormsModule,
    BrowserAnimationsModule,
  ],
  providers: [
    provideHttpClient(withInterceptorsFromDi()),
    Title
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
