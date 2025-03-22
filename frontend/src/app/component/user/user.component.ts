import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { LoginService } from '../../services/session.service';
import { PostService } from '../../services/post.service';
import { UserService } from '../../services/user.service';
import { CommunityService } from '../../services/community.service';
import { User } from '../../models/user.model';
import { Post } from '../../models/post.model';
import { Community } from '../../models/community.model';
import { Chart, registerables } from 'chart.js';
import { DatePipe } from '@angular/common';
import { Ban } from '../../models/ban.model';
import { Router } from '@angular/router';
import { skip, Subscription } from 'rxjs';
import { TitleService } from '../../services/title.service';

Chart.register(...registerables);

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.css', '../../../animations.css'],
  providers: [DatePipe],
})
export class UserComponent implements OnInit, OnDestroy {
  private routeSubscription: Subscription | undefined;
  showModal: boolean = false;
  showUsersModal: boolean = false;
  usersModalMode: number = 0; // 1 -> followers, 2 -> following
  usersModalTitle: string = 'Siguiendo'; // Default title

  showCommunitiesModal: boolean = false;
  communitiesModalMode: boolean = false; // true -> administrated, false -> joined
  communitiesModalTitle: string = 'Comunidades'; // Default title

  usersModalList: User[] = [];
  usersModalListPage: number = 0;
  usersModalListSize: number = 10;
  usersModalListNoMore: boolean = false;
  usersModalListLoading: boolean = false;

  showAdvancedMenu: boolean = false;

  searchTerm: string = '';
  sortCriteria: string = 'default'; // Default search criteria
  sortCriteriaText: string = 'Más antiguos'; // Default search criteria text

  profileUser: User | undefined;

  isProfileOfAdmin: boolean = false;

  posts: Post[] = [];
  postCount: number = 0;

  communities: Community[] = [];
  communityCount: number = 0;
  communitiesPage: number = 0;
  communitiesSize: number = 10;
  communitiesModalListNoMore: boolean = false;
  communitiesModalListLoading: boolean = false;

  title = 'Bookmarks';
  userLoaded = false;

  user: User | undefined;
  loggedUsername: string = '';
  loggedIn: boolean = false;
  isAdmin: boolean = false;

  following: boolean = false;
  followed: boolean = false;

  public chart: any;

  // posts pagination
  size = 10;
  page = 0;

  loadingMorePosts = false;
  noMorePosts = false;

  showAlertModal: boolean = false;
  alertModalText: string = '';
  confirmAction: () => void = () => {};
  showCancelButton: boolean = true;

  // Column toggle properties
  activeColumn: 'main' | 'side' = 'main';
  isMobile: boolean = false;

  constructor(
    private http: HttpClient,
    public loginService: LoginService,
    public profileService: UserService,
    public postService: PostService,
    public communityService: CommunityService,
    private route: ActivatedRoute,
    private router: Router,
    private titleService: TitleService
  ) {
    // Check if mobile on init
    this.checkIfMobile();
    // Listen for window resize events
    window.addEventListener('resize', () => this.checkIfMobile());
  }

  ngOnInit(): void {
    this.titleService.setTitle('Perfil');
    this.checkIfLoggedIn();

    // Subscribe to subsequent route changes
    this.routeSubscription = this.route.params.pipe(skip(1)).subscribe((params) => {
      // Reset the component state
      this.posts = [];
      this.page = 0;
      this.noMorePosts = false;
      this.following = false;
      this.followed = false;

      // Close all modals
      this.showModal = false;
      this.showUsersModal = false;
      this.showCommunitiesModal = false;
      this.showAlertModal = false;

      // Clear search field and reset sorting criteria
      this.searchTerm = '';
      this.sortCriteria = 'default';
      this.sortCriteriaText = 'Más antiguos';

      // Load the new profile data
      this.loadProfile();
    });
  }

  // User profile data (doesn't have to be the logged user)
  loadProfile() {
    let username = this.route.snapshot.paramMap.get('username');
    if (username) {
      this.profileService.getUser(username).subscribe({
        next: (user) => {
          this.profileUser = user;
          this.isProfileOfAdmin = user.roles.includes('ADMIN');
          this.loadPosts();
          this.getPostsCount();
          this.checkFollowing();
          this.titleService.setTitle(`Perfil de ${user.username}`);
        },
        error: (r) => {
          // if error is 404, user is not found, redirect to error page
          if (r.status == 404) {
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Usuario no encontrado',
                description: 'No se ha encontrado el usuario',
                code: 404,
              },
            });
          } else {
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Usuario no encontrado',
                description: r.error.message,
                code: r.status,
              },
            });
          }
        },
      });
    }
  }

  checkFollowing() {
    if (!this.profileUser || !this.loggedIn) {
      return;
    }
    this.profileService
      .isUserFollowing(this.loggedUsername, this.profileUser.username)
      .subscribe({
        next: (following) => {
          this.following = following;
        },
        error: (r) => {
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error al comprobar si sigues al usuario',
              description: r.error.message,
              code: r.status,
            },
          });
        },
      });
    this.profileService
      .isUserFollowing(this.profileUser.username, this.loggedUsername)
      .subscribe({
        next: (followed) => {
          this.followed = followed;
        },
        error: (r) => {
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error al comprobar si el usuario te sigue',
              description: r.error.message,
              code: r.status,
            },
          });
        },
      });
  }

  loadCommunities(admin: boolean) {
    if (this.profileUser) {
      this.profileService
        .getUserCommunities(
          this.profileUser.username,
          admin,
          this.communitiesPage,
          this.communitiesSize
        )
        .subscribe({
          next: (communities) => {
            if (!communities || communities.length == 0) {
              this.communitiesModalListNoMore = true;
              this.communitiesModalListLoading = false;
              return;
            }
            this.communities = communities;
            this.communityCount = communities.length;
            this.communitiesPage += 1;
            this.communitiesModalListNoMore =
              communities.length < this.communitiesSize;
          },
          error: (r) => {
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Error al obtener las comunidades del usuario',
                description: r.error.message,
                code: r.status,
              },
            });
          },
        });
    }
  }

  loadCommunitiesCount(admin: boolean) {
    if (this.profileUser) {
      this.profileService
        .getUserCommunitiesCount(this.profileUser.username, admin)
        .subscribe({
          next: (count) => {
            this.communityCount = count;
          },
          error: (r) => {
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Error al obtener el número de comunidades del usuario',
                description: r.error.message,
                code: r.status,
              },
            });
          },
        });
    }
  }

  // Logged user data
  loadUserData(user: User) {
    this.user = user;
    this.loggedUsername = user.username;
    this.isAdmin = user.roles.includes('ADMIN');
    this.loadProfile();
  }

  loadPosts() {
    if (this.profileUser) {
      this.postService
        .getPostsOfUser(
          this.profileUser.username,
          this.page,
          this.size,
          this.sortCriteria,
          this.searchTerm
        )
        .subscribe({
          next: (posts) => {
            if (!posts || posts.length == 0) {
              this.noMorePosts = true;
              this.loadingMorePosts = false;
              return;
            }
            this.posts = this.posts.concat(posts);
            this.loadingMorePosts = false;
            this.page += 1;
            this.noMorePosts = posts.length < this.size;
          },
          error: (r) => {
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Error al obtener los posts del usuario',
                description: r.error.message,
                code: r.status,
              },
            });
          },
        });
    } else {
      this.router.navigate(['/error'], {
        queryParams: {
          title: 'Usuario no encontrado',
          description: 'No se ha encontrado el usuario',
          code: 404,
        },
      });
    }
  }

  loadMorePosts() {
    this.loadingMorePosts = true;
    this.loadPosts();
    this.loadingMorePosts = false;
  }

  doesPostHaveImage(postID: number) {
    let post: Post | undefined;

    this.postService.getPostById(postID).subscribe({
      next: (p) => {
        post = p;
      },
      error: (r) => {
        this.router.navigate(['/error'], {
          queryParams: {
            title: 'Error al obtener el post',
            description: r.error.message,
            code: r.status,
          },
        });
      },
    });

    if (post) {
      this.postService.getPostImage(postID).subscribe({
        next: () => {
          return true;
        },
        error: (r) => {
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error al obtener la imagen del post',
              description: r.error.message,
              code: r.status,
            },
          });
          return false;
        },
      });
    }
  }

  checkIfLoggedIn() {
    this.loginService.checkLogged().subscribe({
      next: (bool) => {
        this.loggedIn = bool; // set loggedIn to the value returned by the service
        if (bool) {
          // if user is logged in
          this.loginService.getLoggedUser().subscribe({
            // get the logged user
            next: (user) => {
              this.userLoaded = true;
              this.loadUserData(user); // load the user data
            },
            error: (r) => {
              this.router.navigate(['/error'], {
                queryParams: {
                  title: 'Error al obtener el usuario logueado',
                  description: r.error.message,
                  code: r.status,
                },
              });
            },
          });
        } else {
          // if user is not logged in
          this.loggedUsername = ''; // set the logged username to empty
          this.user = undefined;
          this.isAdmin = false;
          this.loadProfile();
        }
      },
      error: (r) => {
        // if error is 401, user is not logged in, do not print error
        if (r.status != 401) {
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error al comprobar si estás logueado',
              description: r.error.message,
              code: r.status,
            },
          });
        }
        this.loadProfile();
      },
    });
  }

  postImage(postID: number) {
    return this.postService.getPostImage(postID);
  }

  postImageURL(postID: number | undefined): string {
    if (!postID) {
      return '';
    }
    return this.postService.getPostImageURL(postID);
  }

  communityBannerURL(communityID: number | undefined): string {
    if (!communityID) {
      return '';
    }
    return this.communityService.getCommunityImageURL(communityID);
  }

  // Get user profile picture
  profilePicture(username: string | undefined): string {
    if (!username) {
      return '';
    }
    return this.profileService.getPostImageURL(username);
  }

  openAlertModal(text: string, action: () => void, showCancel: boolean = true) {
    this.alertModalText = text;
    this.confirmAction = action;
    this.showAlertModal = true;
    this.showCancelButton = showCancel;
  }

  closeAlertModal() {
    this.showAlertModal = false;
  }

  searchPosts() {
    this.posts = [];
    this.page = 0;
    this.noMorePosts = false;
    this.loadingMorePosts = true;
    this.loadPosts();
    this.loadingMorePosts = false;
  }

  clearSearch() {
    this.searchTerm = '';
    this.posts = [];
    this.page = 0;
    this.noMorePosts = false;
    this.loadingMorePosts = true;
    this.loadPosts();
    this.loadingMorePosts = false;
  }

  setSortCriteria(criteria: string) {
    this.sortCriteria = criteria;
    if (criteria === 'lastModifiedDate') {
      this.sortCriteriaText = 'Última modificación';
    } else if (criteria === 'creationDate') {
      this.sortCriteriaText = 'Más recientes';
    } else if (criteria === 'likes') {
      this.sortCriteriaText = 'Mejor votados';
    } else if (criteria === 'replies') {
      this.sortCriteriaText = 'Más comentados';
    } else {
      this.sortCriteriaText = 'Más antiguos';
    }
    this.posts = [];
    this.page = 0;
    this.noMorePosts = false;
    this.loadingMorePosts = true;
    this.loadPosts();
    this.loadingMorePosts = false;
  }

  toggleAdvancedMenu() {
    this.showAdvancedMenu = !this.showAdvancedMenu;
  }

  openModal() {
    this.showModal = true;
    setTimeout(() => {
      const modalElement = document.querySelector('.custom-modal');
      if (modalElement) {
        modalElement.classList.add('show');
      }
    }, 0);
  }

  closeModal() {
    const modalElement = document.querySelector('.custom-modal');
    if (modalElement) {
      modalElement.classList.remove('show');
      setTimeout(() => {
        this.showModal = false;
      }, 300); // Match the duration of the CSS transition
    }
  }

  handleKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      this.openModal();
    } else if (event.key === 'Escape') {
      this.closeModal();
    }
  }

  getPostsCount() {
    if (this.profileUser) {
      this.postService.getPostCountOfUser(this.profileUser.username).subscribe({
        next: (count) => {
          this.postCount = count;
        },
        error: (r) => {
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error al obtener el número de posts del usuario',
              description: r.error.message,
              code: r.status,
            },
          });
        },
      });
    }
  }

  loadFollowing() {
    if (this.profileUser) {
      this.profileService
        .getFollowing(
          this.profileUser.username,
          this.usersModalListPage,
          this.usersModalListSize
        )
        .subscribe({
          next: (users) => {
            if (!users || users.length == 0) {
              this.usersModalListNoMore = true;
              this.usersModalListLoading = false;
              return;
            }
            this.usersModalList = this.usersModalList.concat(users);
            this.usersModalListLoading = false;
            this.usersModalListPage += 1;
            this.usersModalListNoMore = users.length < this.usersModalListSize;
          },
          error: (r) => {
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Error al obtener los usuarios seguidos',
                description: r.error.message,
                code: r.status,
              },
            });
          },
        });
    }
  }

  loadFollowers() {
    if (this.profileUser) {
      this.profileService
        .getFollowers(
          this.profileUser.username,
          this.usersModalListPage,
          this.usersModalListSize
        )
        .subscribe({
          next: (users) => {
            if (!users || users.length == 0) {
              this.usersModalListNoMore = true;
              this.usersModalListLoading = false;
              return;
            }
            this.usersModalList = this.usersModalList.concat(users);
            this.usersModalListLoading = false;
            this.usersModalListPage += 1;
            this.usersModalListNoMore = users.length < this.usersModalListSize;
          },
          error: (r) => {
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Error al obtener los seguidores',
                description: r.error.message,
                code: r.status,
              },
            });
          },
        });
    }
  }

  loadUsersList(mode: number) {
    if (mode == 1) {
      this.usersModalTitle = 'Seguidores';
      this.loadFollowers();
    } else if (mode == 2) {
      this.usersModalTitle = 'Siguiendo';
      this.loadFollowing();
    }
  }

  loadMoreUsers() {
    this.usersModalListLoading = true;

    if (this.usersModalMode == 1) {
      this.loadFollowers();
    } else if (this.usersModalMode == 2) {
      this.loadFollowing();
    }

    this.usersModalListLoading = false;
  }

  openUsersModal(mode: number) {
    if (!this.profileUser) {
      return;
    }

    // mode = 1 -> followers, mode = 2 -> following
    this.usersModalList = [];
    this.usersModalListPage = 0;
    this.usersModalListNoMore = false;
    this.usersModalListLoading = false;
    this.showUsersModal = true;
    this.usersModalMode = mode;
    this.loadUsersList(mode);
  }

  closeUsersModal() {
    this.showUsersModal = false;
  }

  openCommunitiesModal(admin: boolean) {
    if (!this.profileUser) {
      return;
    }

    // mode = 1 -> administrated, mode = 2 -> joined
    if (admin) {
      this.communitiesModalTitle = 'Comunidades administradas:';
    } else {
      this.communitiesModalTitle = 'Comunidades a las que pertenece:';
    }
    this.communities = [];
    this.communitiesPage = 0;
    this.communitiesSize = 10;
    this.showCommunitiesModal = true;
    this.communitiesModalMode = admin;
    this.loadCommunities(admin);
    this.loadCommunitiesCount(admin);
  }

  closeCommunitiesModal() {
    this.showCommunitiesModal = false;
  }

  loadMoreCommunities() {
    this.communitiesModalListLoading = true;

    if (this.communitiesModalMode) {
      this.loadCommunities(true);
    } else {
      this.loadCommunities(false);
    }

    this.communitiesModalListLoading = false;
  }

  followUser() {
    if (!this.profileUser) {
      return;
    }
    this.profileService
      .followUser(this.loggedUsername, this.profileUser.username)
      .subscribe({
        next: () => {
          if (!this.profileUser) {
            return;
          }
          this.profileUser.followers += 1;
          this.following = true;
        },
        error: (r) => {
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error al seguir al usuario',
              description: r.error.message,
              code: r.status,
            },
          });
        },
      });
  }

  unfollowUser() {
    if (!this.profileUser) {
      return;
    }
    this.profileService
      .unfollowUser(this.loggedUsername, this.profileUser.username)
      .subscribe({
        next: () => {
          if (!this.profileUser) {
            return;
          }
          this.profileUser.followers -= 1;
          this.following = false;
        },
        error: (r) => {
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error al dejar de seguir al usuario',
              description: r.error.message,
              code: r.status,
            },
          });
        },
      });
  }

  deleteUser() {
    if (!this.profileUser) {
      return;
    }
    //show confirmation modal
    this.openAlertModal(
      '¿Estás seguro de que quieres cerrar y eliminar esta cuenta? Esta acción no se puede deshacer.',
      () => {
        if (!this.profileUser) {
          return;
        }
        this.profileService.deleteUser(this.profileUser.username).subscribe({
          next: () => {
            this.closeAlertModal();
            //redirect to home
            window.location.href = '/';
          },
          error: (r) => {
            // if error is unauthorized, show modal
            if (r.status == 401) {
              this.openAlertModal(
                'No puedes realizar esta acción. Si eres el administrador del sitio, no puedes cerrar tu cuenta.',
                () => {
                  this.closeAlertModal();
                },
                false
              );
            } else if (r.status == 403) {
              this.openAlertModal(
                'No puedes cerrar tu cuenta: aún eres administrador de una o más comunidades. Deja de ser administrador de todas las comunidades antes de cerrar tu cuenta.',
                () => {
                  this.closeAlertModal();
                },
                false
              );
              return;
            } else {
              this.router.navigate(['/error'], {
                queryParams: {
                  title: 'Error al eliminar la cuenta',
                  description: r.error.message,
                  code: r.status,
                },
              });
            }
          },
        });
      },
      true
    );
  }

  // Column toggle methods
  setActiveColumn(column: 'main' | 'side') {
    this.activeColumn = column;
  }

  private checkIfMobile() {
    this.isMobile = window.innerWidth < 992; // 992px is Bootstrap's lg breakpoint
  }

  ngOnDestroy() {
    // Clean the subscription when the component is destroyed
    if (this.routeSubscription) {
      this.routeSubscription.unsubscribe();
    }
    // Remove resize listener when component is destroyed
    window.removeEventListener('resize', () => this.checkIfMobile());
  }
}
