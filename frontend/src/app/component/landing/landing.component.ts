import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { LoginService } from '../../services/session.service';
import { PostService } from '../../services/post.service';
import { UserService } from '../../services/user.service';
import { CommunityService } from '../../services/community.service';
import { User } from '../../models/user.model';
import { Post } from '../../models/post.model';
import { Community } from '../../models/community.model';
import { Chart, registerables } from 'chart.js';
import { DatePipe } from '@angular/common';

Chart.register(...registerables);

@Component({
  selector: 'app-landing',
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.css', '../../../animations.css'],
  providers: [DatePipe],
})
export class LandingComponent implements OnInit {
  title = 'Bookmarks';
  totalSiteBooks: number | undefined;
  totalSiteGenres: number | undefined;
  totalSiteAuthors: number | undefined;
  totalSiteUsers: number | undefined;
  heroNameVisible = false;

  userLoaded = false;

  communitiesMembersCount: [number, string, number][] = [];

  recommendedPostsPopularUsers: Post[] = [];
  recommendedPostsPopularCommunities: Post[] = [];

  user: User | undefined;
  loggedUsername: string = '';
  loggedIn: boolean = false;
  isAdmin: boolean = false;

  loadingChart = true;
  public chart: any;

  size = 4;
  pagePostUsers = 0;
  pagePostCommunities = 0;

  loadingMorePostsUsers = false;
  loadingMorePostsCommunities = false;

  noMorePostsUsers = false;
  noMorePostsCommunities = false;

  latestGeneralPosts: Post[] = [];

  constructor(
    private http: HttpClient,
    public loginService: LoginService,
    public profileService: UserService,
    public postService: PostService,
    public communityService: CommunityService
  ) {}

  ngOnInit(): void {
    // This is done to ensure that the URL does not end with a trailing slash so that refreshing the landing page works
    let url = window.location.href;
    url = url.substring(0, url.length - 1);
    history.pushState(null, '', url);
    this.loadChart();
    this.loadPopularCommunitiesList();

    // Check if user is logged in
    this.checkIfLoggedIn();
  }

  loadPopularCommunitiesList() {
    this.communityService.getMostPopularCommunitiesMembersCount(10).subscribe({
      next: (communities) => {
        this.communitiesMembersCount = communities;
      },
      error: (r) => {
        console.error(
          'Error getting most popular communities members count: ' +
            JSON.stringify(r)
        );
      },
    });
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
    if (option === 'default' || option === 'most-liked-users') {
      this.postService
        .getUserRecommendations(
          'most-liked-users',
          this.pagePostUsers,
          this.size
        )
        .subscribe({
          next: (posts) => {
            if (!posts) {
              this.noMorePostsUsers = true;
              this.loadingMorePostsUsers = false;
              return;
            }
            this.recommendedPostsPopularUsers =
              this.recommendedPostsPopularUsers.concat(posts);
            this.pagePostUsers += 1;
            this.loadingMorePostsUsers = false;

            if (posts.length < this.size) {
              this.noMorePostsUsers = true;
            }
          },
          error: (r) => {
            console.error(
              'Error getting recommended posts: ' + JSON.stringify(r)
            );
          },
        });
    } else if (option === 'most-recent-communities' || option === 'default') {
      this.postService
        .getUserRecommendations(
          'most-recent-communities',
          this.pagePostCommunities,
          this.size
        )
        .subscribe({
          next: (posts) => {
            if (!posts) {
              this.noMorePostsCommunities = true;
              this.loadingMorePostsCommunities = false;
              return;
            }
            this.recommendedPostsPopularCommunities =
              this.recommendedPostsPopularCommunities.concat(posts);
            this.pagePostCommunities += 1;
            this.loadingMorePostsCommunities = false;

            if (posts.length < this.size) {
              this.noMorePostsCommunities = true;
            }
          },
          error: (r) => {
            console.error(
              'Error getting recommended posts: ' + JSON.stringify(r)
            );
          },
        });
    } else if (option === 'no-following' || option === 'most-liked-users') {
      this.postService
        .getGeneralRecommendations(
          'most-liked-users',
          this.pagePostUsers,
          this.size
        )
        .subscribe({
          next: (posts) => {
            if (!posts) {
              this.noMorePostsUsers = true;
              this.loadingMorePostsUsers = false;
              return;
            }
            this.recommendedPostsPopularUsers =
              this.recommendedPostsPopularUsers.concat(posts);
            this.pagePostUsers += 1;
            this.loadingMorePostsUsers = false;

            if (posts.length < this.size) {
              this.noMorePostsUsers = true;
            }
          },
          error: (r) => {
            console.error(
              'Error getting recommended posts: ' + JSON.stringify(r)
            );
          },
        });
    } else if (
      option === 'most-liked-communities' ||
      option === 'no-following'
    ) {
      this.postService
        .getUserRecommendations(
          'most-recent-communities',
          this.pagePostCommunities,
          this.size
        )
        .subscribe({
          next: (posts) => {
            if (!posts) {
              this.noMorePostsCommunities = true;
              this.loadingMorePostsCommunities = false;
              return;
            }
            this.recommendedPostsPopularCommunities =
              this.recommendedPostsPopularCommunities.concat(posts);
            this.pagePostCommunities += 1;
            this.loadingMorePostsCommunities = false;

            if (posts.length < this.size) {
              this.noMorePostsCommunities = true;
            }
          },
          error: (r) => {
            console.error(
              'Error getting recommended posts: ' + JSON.stringify(r)
            );
          },
        });
    } else {
      this.postService
        .getUserRecommendations(
          'most-liked-communities',
          this.pagePostCommunities,
          this.size
        )
        .subscribe({
          next: (posts) => {
            if (!posts) {
              this.noMorePostsCommunities = true;
              this.loadingMorePostsCommunities = false;
              return;
            }
            this.recommendedPostsPopularCommunities =
              this.recommendedPostsPopularCommunities.concat(posts);
            this.pagePostCommunities += 1;
            this.loadingMorePostsCommunities = false;

            if (posts.length < this.size) {
              this.noMorePostsCommunities = true;
            }
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
    if (option === 'most-liked-users' || option === 'default') {
      this.postService
        .getGeneralRecommendations(
          'most-liked-users', // default
          this.pagePostUsers,
          this.size
        )
        .subscribe({
          next: (posts) => {
            // if response is 204, no posts were found
            if (!posts) {
              this.noMorePostsUsers = true;
              this.loadingMorePostsUsers = false;
              return;
            }
            this.recommendedPostsPopularUsers =
              this.recommendedPostsPopularUsers.concat(posts);
            this.pagePostUsers += 1;
            this.loadingMorePostsUsers = false;

            if (posts.length < this.size) {
              this.noMorePostsUsers = true;
            }
          },
          error: (r) => {
            console.error(
              'Error getting recommended posts: ' + JSON.stringify(r)
            );
          },
        });
    } else if (option === 'most-recent-communities' || option === 'default') {
      this.postService
        .getGeneralRecommendations(
          'most-recent-communities', // default
          this.pagePostCommunities,
          this.size
        )
        .subscribe({
          next: (posts) => {
            if (!posts) {
              this.noMorePostsCommunities = true;
              this.loadingMorePostsCommunities = false;
              return;
            }
            this.recommendedPostsPopularCommunities =
              this.recommendedPostsPopularCommunities.concat(posts);
            this.pagePostCommunities += 1;
            this.loadingMorePostsCommunities = false;

            if (posts.length < this.size) {
              this.noMorePostsCommunities = true;
            }
          },
          error: (r) => {
            console.error(
              'Error getting recommended posts: ' + JSON.stringify(r)
            );
          },
        });
    } else {
      this.postService
        .getGeneralRecommendations(
          'most-liked-communities',
          this.pagePostCommunities,
          this.size
        )
        .subscribe({
          next: (posts) => {
            if (!posts) {
              this.noMorePostsCommunities = true;
              this.loadingMorePostsCommunities = false;
              return;
            }
            this.recommendedPostsPopularCommunities =
              this.recommendedPostsPopularCommunities.concat(posts);
            this.pagePostCommunities += 1;
            this.loadingMorePostsCommunities = false;

            if (posts.length < this.size) {
              this.noMorePostsCommunities = true;
            }
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
        this.algoUser('most-liked-users');
        this.algoUser('most-recent-communities');
      } else {
        this.algoUser('no-following');
        this.algoUser('most-liked-users');
        this.algoUser('most-liked-communities');
      }
    } else {
      // if user is not logged in
      this.algoGeneral('most-liked-users');
      this.algoGeneral('most-recent-communities');
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
    return this.profileService.getPostImageURL(username);
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

  postImage(postID: number) {
    return this.postService.getPostImage(postID);
  }

  postImageURL(postID: number) {
    return this.postService.getPostImageURL(postID);
  }

  loadChart() {
    // Chart data gathering

    let usernames: string[] = [];
    let likesCount: number[] = [];

    this.profileService.getMostPopularUsersLikesCount(10).subscribe({
      next: (users) => {
        users.forEach((user) => {
          usernames.push(user[0]);
          likesCount.push(user[1]);
        });

        // Create the chart

        new Chart('mostPopularUsersChart', {
          type: 'bar',
          data: {
            labels: usernames,
            datasets: [
              {
                label: 'Upvotes totales',
                data: likesCount,
                backgroundColor: '#F8DDA4',
                borderWidth: 1,
              },
            ],
          },
          options: {
            scales: {
              y: {
                beginAtZero: true,
                ticks: {
                  callback: function (value, index, values) {
                    return Math.round(value as number);
                  },
                  color: '#91818A', // Set the color of the y-axis labels
                },
                grid: {
                  display: false,
                },
              },
              x: {
                ticks: {
                  color: '#efefef', // Set the color of the x-axis labels
                },
                grid: {
                  display: false,
                },
              },
            },
            plugins: {
              legend: {
                labels: {
                  color: '#efefef', // Set the color of the legend text
                },
              },
            },
          },
        });
        this.loadingChart = false;
      },
      error: (r) => {
        console.error(
          'Error getting most popular users likes count: ' + JSON.stringify(r)
        );
      },
    });
  }

  loadMorePostsGeneral(mode: number) {
    if (mode === 0) {
      this.loadingMorePostsUsers = true;
      this.algoGeneral('most-liked-users');
    }
    if (mode === 1) {
      this.loadingMorePostsCommunities = true;
      this.algoGeneral('most-recent-communities');
    }
  }

  loadMorePostsUser(mode: number) {
    if (mode === 0) {
      this.loadingMorePostsUsers = true;
      this.algoUser('most-liked-users');
    }
    if (mode === 1) {
      this.loadingMorePostsCommunities = true;
      this.algoUser('most-recent-communities');
    }
  }

  /*
        mode: 0: load more posts for users
              1: load more posts for communities
  */
  loadMorePosts(mode: number) {
    if (!this.loggedIn) {
      this.loadMorePostsGeneral(mode);
    } else {
      this.loadMorePostsUser(mode);
    }
  }
}