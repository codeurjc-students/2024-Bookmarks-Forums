import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LandingComponent } from './component/landing/landing.component';
import { LoginComponent } from './component/login/login.component';
import { SignupComponent } from './component/signUp/signup.component';
import { PostComponent } from './component/post/post.component';
import { ModifyPostComponent } from './component/modifyPost/modifyPost.component';
import { CommunityComponent } from './component/community/community.component';
import { ModifyCommunityComponent } from './component/modifyCommunity/modifyCommunity.component';
import { NewCommunityComponent } from './component/newCommunity/newCommunity.component';
import { NewPostComponent } from './component/newPost/newPost.component';
import { UserComponent } from './component/user/user.component';
import { ModifyUserComponent } from './component/modifyUser/modifyUser.component';

const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
    data: { animation: 'LoginPage' },
  },
  {
    path: 'signup',
    component: SignupComponent,
    data: { animation: 'SignupPage' },
  },
  { path: '', component: LandingComponent },
  { path: 'post/:identifier', component: PostComponent },
  { path: 'post/:identifier/edit', component: ModifyPostComponent },
  { path: 'community/:identifier', component: CommunityComponent },
  { path: 'community/:identifier/edit', component: ModifyCommunityComponent },
  { path: 'new/community', component: NewCommunityComponent },
  { path: 'community/:identifier/new/post', component: NewPostComponent },
  { path: 'profile/:username', component: UserComponent },
  { path: 'profile/:username/edit', component: ModifyUserComponent },
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, { scrollPositionRestoration: 'enabled' }),
  ],
  exports: [RouterModule],
})
export class AppRoutingModule {}
