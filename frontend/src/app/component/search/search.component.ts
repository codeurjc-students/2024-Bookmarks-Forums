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
import { Location } from '@angular/common';
import { Router } from '@angular/router';
import { TitleService } from '../../services/title.service';
Chart.register(...registerables);

@Component({
  selector: 'app-community',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css', '../../../animations.css'],
  providers: [DatePipe],
})
export class SearchComponent implements OnInit, OnDestroy {
  showModal: boolean = false;

  showAdvancedMenu: boolean = false;

  title = 'Bookmarks';
  userLoaded = false;

  user: User | undefined;
  loggedUsername: string = '';
  loggedIn: boolean = false;
  isAdmin: boolean = false;

  public chart: any;

  showAlertModal: boolean = false;
  alertModalText: string = '';
  confirmAction: () => void = () => {};
  showCancelButton: boolean = true;

  // Search term
  searchTerm: string = '';

  searchTitle: string = '';
  searching: boolean = false;

  // Posts
  posts: Post[] = [];
  postsPage: number = 0;
  postsSize: number = 10;
  noMorePosts: boolean = false;
  loadingMorePosts: boolean = false;
  postSortCriteria: string = 'default'; // creationDate, lastModifiedDate, replies, likes
  postSortCriteriaText: string = 'Más antiguos';

  // Communities
  communities: Community[] = [];
  communitiesPage: number = 0;
  communitiesSize: number = 10;
  noMoreCommunities: boolean = false;
  loadingMoreCommunities: boolean = false;
  communitySortCriteria: string = 'default'; // creationDate, members, lastPostDate
  communitySortCriteriaText: string = 'Más antiguas';
  communityFilter: string = 'default'; // can search by name, or description + name
  communityFilterText: string = 'Nombre y descripción';

  // Users
  users: User[] = [];
  usersPage: number = 0;
  usersSize: number = 10;
  noMoreUsers: boolean = false;
  loadingMoreUsers: boolean = false;
  userSortCriteria: boolean = false; // true = newest, false = alphabetical
  userSortCriteriaText: string = 'Alfabético';

  // Column toggle properties
  activeColumn: 'posts' | 'communities' | 'users' = 'posts';
  isMobile: boolean = false;
  pillWidth: string = '0px';
  pillOffset: string = '0px';

  constructor(
    private http: HttpClient,
    public loginService: LoginService,
    public profileService: UserService,
    public postService: PostService,
    public communityService: CommunityService,
    private route: ActivatedRoute,
    private location: Location,
    private router: Router,
    private titleService: TitleService
  ) {
    // Check if mobile on init
    this.checkIfMobile();
    // Listen for window resize events
    window.addEventListener('resize', () => this.checkIfMobile());
  }

  ngOnInit(): void {
    this.titleService.setTitle('Explorar');
    this.route.queryParams.subscribe((params) => {
      if (!this.searchTerm) {
        this.searchTerm = params['query'] || '';
      }
    });
    this.checkIfLoggedIn();
  }

  startSearch() {
    this.searching = true;
    this.search();
  }

  clearSearch() {
    this.searchTerm = '';
    this.location.replaceState('/search', '');
    this.searching = false;
    this.search();
  }

  loadUserData(user: User) {
    this.user = user;
    this.loggedUsername = user.username;
    this.isAdmin = user.roles.includes('ADMIN');
    this.startSearch();
  }

  loadPosts() {
    this.postService
      .searchPosts(
        this.searchTerm,
        this.postsPage,
        this.postsSize,
        this.postSortCriteria
      )
      .subscribe({
        next: (posts) => {
          if (!posts || posts.length === 0) {
            this.noMorePosts = true;
            this.loadingMorePosts = false;
            return;
          }
          this.posts = this.posts.concat(posts);
          this.loadingMorePosts = false;
          this.postsPage += 1;
          this.noMorePosts = posts.length < this.postsSize;
        },
        error: (r) => {
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error al cargar posts',
              description: r.error.message,
              code: r.status,
            },
          });
        },
      });
  }

  loadCommunities() {
    this.communityService
      .searchCommunities(
        this.searchTerm,
        this.communitiesPage,
        this.communitiesSize,
        this.communitySortCriteria,
        this.communityFilter
      )
      .subscribe({
        next: (communities) => {
          if (!communities || communities.length === 0) {
            this.noMoreCommunities = true;
            this.loadingMoreCommunities = false;
            return;
          }
          this.communities = this.communities.concat(communities);
          this.loadingMoreCommunities = false;
          this.communitiesPage += 1;
          this.noMoreCommunities = communities.length < this.communitiesSize;
        },
        error: (r) => {
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error al cargar comunidades',
              description: r.error.message,
              code: r.status,
            },
          });
        },
      });
  }

  loadUsers() {
    this.profileService
      .searchUsers(
        this.searchTerm,
        this.usersPage,
        this.usersSize,
        this.userSortCriteria
      )
      .subscribe({
        next: (users) => {
          if (!users || users.length === 0) {
            this.noMoreUsers = true;
            this.loadingMoreUsers = false;
            return;
          }
          this.users = this.users.concat(users);
          this.loadingMoreUsers = false;
          this.usersPage += 1;
          this.noMoreUsers = users.length < this.usersSize;
        },
        error: (r) => {
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error al cargar usuarios',
              description: r.error.message,
              code: r.status,
            },
          });
        },
      });
  }

  loadMorePosts() {
    this.loadingMorePosts = true;
    this.loadPosts();
    this.loadingMorePosts = false;
  }

  loadMoreCommunities() {
    this.loadingMoreCommunities = true;
    this.loadCommunities();
    this.loadingMoreCommunities = false;
  }

  loadMoreUsers() {
    this.loadingMoreUsers = true;
    this.loadUsers();
    this.loadingMoreUsers = false;
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
            title: 'Error al cargar post',
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
              title: 'Error al cargar imagen de post',
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
                  title: 'Error al cargar usuario',
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
          this.startSearch();
        }
      },
      error: (r) => {
        // if error is 401, user is not logged in, do not print error
        if (r.status != 401) {
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error al comprobar si está logueado',
              description: r.error.message,
              code: r.status,
            },
          });
        }
        this.startSearch();
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

  reSearch(mode: number) {
    if (mode === 1) {
      this.posts = [];
      this.postsPage = 0;
      this.noMorePosts = false;
      this.loadingMorePosts = false;
      this.loadPosts();
    } else if (mode === 2) {
      this.communities = [];
      this.communitiesPage = 0;
      this.noMoreCommunities = false;
      this.loadingMoreCommunities = false;
      this.loadCommunities();
    } else if (mode === 3) {
      this.users = [];
      this.usersPage = 0;
      this.noMoreUsers = false;
      this.loadingMoreUsers = false;
      this.loadUsers();
    }
  }

  setSortCriteria(category: number, criteria: string) {
    if (category === 1) {
      this.postSortCriteria = criteria;
      switch (criteria) {
        case 'creationDate':
          this.postSortCriteriaText = 'Más recientes';
          break;
        case 'replies':
          this.postSortCriteriaText = 'Más comentarios';
          break;
        case 'likes':
          this.postSortCriteriaText = 'Más votados';
          break;
        default:
          this.postSortCriteriaText = 'Más antiguos';
          break;
      }
      this.reSearch(1);
    } else if (category === 2) {
      this.communitySortCriteria = criteria;
      switch (criteria) {
        case 'creationDate':
          this.communitySortCriteriaText = 'Más recientes';
          break;
        case 'members':
          this.communitySortCriteriaText = 'Más miembros';
          break;
        case 'lastPostDate':
          this.communitySortCriteriaText = 'Último post';
          break;
        default:
          this.communitySortCriteriaText = 'Más antiguas';
          break;
      }
      this.reSearch(2);
    } else if (category === 3) {
      this.userSortCriteria = criteria === 'newest';
      this.userSortCriteriaText =
        criteria === 'newest' ? 'Más recientes' : 'Alfabético';
      this.reSearch(3);
    }
  }

  setFilterCriteria(criteria: string) {
    this.communityFilter = criteria;
    switch (criteria) {
      case 'name':
        this.communityFilterText = 'Solo nombre';
        break;
      case 'description':
        this.communityFilterText = 'Solo descripción';
        break;
      default:
        this.communityFilterText = 'Nombre y descripción';
        break;
    }
    this.reSearch(2);
  }

  search() {
    if (this.searchTerm.trim() === '') {
      this.titleService.setTitle('Explorar');
      this.searchTitle = 'Aquí tienes todo Bookmarks Forums';
    } else {
      this.titleService.setTitle(`Explorar: "${this.searchTerm}"`);
      this.searchTitle = 'Resultados de la búsqueda para: "' + this.searchTerm + '"';
    }

    // Add query to URL if searchTerm is not empty
    if (this.searchTerm.trim() !== '') {
      this.location.replaceState('/search', '?query=' + this.searchTerm);
    } else {
      this.location.replaceState('/search', '');
    }

    this.posts = [];
    this.postsPage = 0;
    this.noMorePosts = false;
    this.loadingMorePosts = false;
    this.communities = [];
    this.communitiesPage = 0;
    this.noMoreCommunities = false;
    this.loadingMoreCommunities = false;
    this.users = [];
    this.usersPage = 0;
    this.noMoreUsers = false;
    this.loadingMoreUsers = false;

    this.loadPosts();
    this.loadCommunities();
    this.loadUsers();
  }

  // Column toggle methods
  setActiveColumn(column: 'posts' | 'communities' | 'users') {
    this.activeColumn = column;
    // Use requestAnimationFrame to ensure DOM is updated
    requestAnimationFrame(() => {
      // Add a small delay to ensure iOS has processed the DOM changes
      setTimeout(() => {
        this.updatePillDimensions();
      }, 50);
    });
  }

  private checkIfMobile() {
    this.isMobile = window.innerWidth < 992; // 992px is Bootstrap's lg breakpoint
    if (this.isMobile) {
      // Use requestAnimationFrame for mobile check as well
      requestAnimationFrame(() => {
        setTimeout(() => {
          this.updatePillDimensions();
        }, 50);
      });
    }
  }

  private updatePillDimensions() {
    const activeButton = document.querySelector('.toggle-btn.active') as HTMLElement;
    if (activeButton) {
      // Force a reflow to ensure dimensions are up to date
      activeButton.offsetHeight;
      
      const width = activeButton.offsetWidth;
      const left = activeButton.offsetLeft;
      
      // Update both properties at once to ensure smooth animation
      this.pillWidth = `${width}px`;
      this.pillOffset = `${left}px`;
    }
  }

  ngOnDestroy() {
    // Remove resize listener when component is destroyed
    window.removeEventListener('resize', () => this.checkIfMobile());
  }
}
