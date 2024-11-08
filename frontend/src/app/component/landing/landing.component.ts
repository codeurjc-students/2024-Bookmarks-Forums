import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { LoginService } from '../../services/session.service';
import { PostService } from '../../services/post.service';
import { UserService } from '../../services/user.service';
import { User } from '../../models/user.model';
import { Post } from '../../models/post.model';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

@Component({
  selector: 'app-landing',
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.css', '../../../animations.css'],
})
export class LandingComponent implements OnInit {
  title = 'Bookmarks';
  totalSiteBooks: number | undefined;
  totalSiteGenres: number | undefined;
  totalSiteAuthors: number | undefined;
  totalSiteUsers: number | undefined;
  heroNameVisible = false;

  userLoaded = false;

  recommendedPostsPopularUsers: Post[] = [];
  recommendedPostsPopularCommunities: Post[] = [];

  user: User | undefined;
  loggedUsername: string = '';
  loggedIn: boolean = false;
  isAdmin: boolean = false;

  latestGeneralPosts: Post[] = [];

  constructor(
    private http: HttpClient,
    public loginService: LoginService,
    public profileService: UserService,
    public postService: PostService
  ) {}

  ngOnInit(): void {
    // This is done to ensure that the URL does not end with a trailing slash so that refreshing the landing page works
    let url = window.location.href;
    url = url.substring(0, url.length - 1);
    history.pushState(null, '', url);

    // Check if user is logged in
    this.checkIfLoggedIn();
  }

  loadUserData(user: User) {
    this.user = user;
    this.loggedUsername = user.username;
    this.isAdmin = user.roles.includes('ADMIN');
  }

  doesPostHaveImage(postID: number) {
    let post: Post | undefined;

    this.postService.getPostById(postID).subscribe({
      next: (p) => {
        post = p;
      },
      error: (r) => {
        console.error('Error getting post: ' + JSON.stringify(r));
      },
    });

    if (post) {
      this.postService.getPostImage(postID).subscribe({
        next: () => {
          return true;
        },
        error: (r) => {
          console.error('Error getting post image: ' + JSON.stringify(r));
          return false;
        },
      });
    }
  }

  algoUser(option: string) {
    if (option === 'default') {
      this.postService.getUserRecommendations('most-liked-users').subscribe({
        next: (posts) => {
          this.recommendedPostsPopularUsers = posts;
        },
        error: (r) => {
          console.error(
            'Error getting recommended posts: ' + JSON.stringify(r)
          );
        },
      });
      this.postService
        .getUserRecommendations('most-recent-communities')
        .subscribe({
          next: (posts) => {
            this.recommendedPostsPopularCommunities = posts;
          },
          error: (r) => {
            console.error(
              'Error getting recommended posts: ' + JSON.stringify(r)
            );
          },
        });
    } else if (option === 'no-following') {
      this.postService.getGeneralRecommendations('most-liked-users').subscribe({
        next: (posts) => {
          this.recommendedPostsPopularUsers = posts;
        },
        error: (r) => {
          console.error(
            'Error getting recommended posts: ' + JSON.stringify(r)
          );
        },
      });
      this.postService
        .getUserRecommendations('most-recent-communities')
        .subscribe({
          next: (posts) => {
            this.recommendedPostsPopularCommunities = posts;
          },
          error: (r) => {
            console.error(
              'Error getting recommended posts: ' + JSON.stringify(r)
            );
          },
        });
    } else {
      this.postService
        .getUserRecommendations('most-liked-communities')
        .subscribe({
          next: (posts) => {
            this.recommendedPostsPopularCommunities = posts;
          },
          error: (r) => {
            console.error(
              'Error getting recommended posts: ' + JSON.stringify(r)
            );
          },
        });
    }
  }

  algoGeneral(option: string) {
    this.postService.getGeneralRecommendations('most-liked-users').subscribe({
      next: (posts) => {
        this.recommendedPostsPopularUsers = posts;
      },
      error: (r) => {
        console.error('Error getting recommended posts: ' + JSON.stringify(r));
      },
    });

    if (option === 'default') {
      this.postService
        .getGeneralRecommendations('most-recent-communities')
        .subscribe({
          next: (posts) => {
            this.recommendedPostsPopularCommunities = posts;
          },
          error: (r) => {
            console.error(
              'Error getting recommended posts: ' + JSON.stringify(r)
            );
          },
        });
    } else {
      this.postService
        .getGeneralRecommendations('most-liked-communities')
        .subscribe({
          next: (posts) => {
            this.recommendedPostsPopularCommunities = posts;
          },
          error: (r) => {
            console.error(
              'Error getting recommended posts: ' + JSON.stringify(r)
            );
          },
        });
    }
  }

  loadLists() {
    // if user is logged in
    if (this.loggedIn) {
      // does the user follow anyone
      if (this.user!.following > 0) {
        this.algoUser('default');
      } else {
        this.algoUser('no-following');
      }
    } else {
      // if user is not logged in
      this.algoGeneral('default');
    }
  }

  checkIfLoggedIn() {
    this.loginService.checkLogged().subscribe({
      next: (bool) => {
        this.loggedIn = bool; // set loggedIn to the value returned by the service
        if (bool) {
          // if user is logged in
          this.heroNameVisible = true;
          this.loginService.getLoggedUser().subscribe({
            // get the logged user
            next: (user) => {
              this.userLoaded = true;
              this.loadUserData(user); // load the user data
              this.loadLists();
            },
            error: (r) => {
              console.error('Error getting logged user: ' + JSON.stringify(r));
            },
          });
        } else {
          // if user is not logged in
          this.loggedUsername = ''; // set the logged username to empty
          this.user = undefined;
          this.isAdmin = false;
          this.loadLists();
        }
      },
      error: (r) => {
        // if error is 401, user is not logged in, do not print error
        if (r.status != 401) {
          console.error(
            'Error checking if user is logged in: ' + JSON.stringify(r)
          );
        }
      },
    });
  }

  // Get user profile picture
  profilePicture(username: string) {
    return this.profileService.downloadProfilePicture(username);
  }

  login() {
    this.loginService
      .login({ username: 'YourReader', password: 'pass' })
      .subscribe({
        next: (r) => {
          // reload the page
          window.location.reload();
        },
        error: (r) => {
          console.error('Login failed: ' + JSON.stringify(r));
        },
      });
  }

  logout() {
    this.loginService.logout().subscribe({
      next: (r) => {
        console.log('Logout successful');
        // reload the page
        window.location.reload();
      },
      error: (r) => {
        console.error('Logout failed: ' + JSON.stringify(r));
      },
    });
  }
}
