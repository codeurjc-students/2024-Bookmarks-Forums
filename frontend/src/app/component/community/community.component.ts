import { Component, OnInit } from '@angular/core';
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

Chart.register(...registerables);

@Component({
  selector: 'app-community',
  templateUrl: './community.component.html',
  styleUrls: ['./community.component.css', '../../../animations.css'],
  providers: [DatePipe],
})
export class CommunityComponent implements OnInit {
  showModal: boolean = false;

  selectedOrderText: string = 'Fecha de creación'; // Default text

  showAdvancedMenu: boolean = false;

  searchTerm: string = '';
  searchCriteria: string = 'default'; // Default search criteria (can be title, content or author)
  searchCriteriaText: string = 'Título + Contenido'; // Default search criteria text

  communityPostsCount: number = 0;
  posts: Post[] = [];

  community: Community | undefined;
  communityMembersCount: number = 0;
  communityMembers: User[] = [];

  title = 'Bookmarks';
  userLoaded = false;

  user: User | undefined;
  loggedUsername: string = '';
  loggedIn: boolean = false;
  isAdmin: boolean = false;

  public chart: any;

  // posts pagination
  size = 10;
  page = 0;
  postOrder = 'lastModifiedDate'; // order by last modified date by default. Can be changed to 'creationDate', 'likes' or 'replies'

  loadingMorePosts = false;

  noMorePosts = false;

  showAlertModal: boolean = false;
  alertModalText: string = '';
  confirmAction: () => void = () => {};

  constructor(
    private http: HttpClient,
    public loginService: LoginService,
    public profileService: UserService,
    public postService: PostService,
    public communityService: CommunityService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Check if user is logged in
    this.checkIfLoggedIn();
    this.loadCommunity();
  }

  loadUserData(user: User) {
    this.user = user;
    this.loggedUsername = user.username;
    this.isAdmin = user.roles.includes('ADMIN');
  }

  

  loadCommunity() {
    let communityID = Number(this.route.snapshot.paramMap.get('identifier'));
    this.communityService.getCommunityById(communityID).subscribe({
      next: (community) => {
        this.community = community;
        this.getMembers();
        this.getMembersCount();
        this.getPostsCount();
        this.loadPosts();
      },
      error: (r) => {
        console.error('Error getting community: ' + JSON.stringify(r));
      },
    });
  }

  loadPosts() {
    if (this.community) {
      this.communityService
        .getPosts(
          this.community.identifier,
          this.page,
          this.size,
          this.postOrder
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
            console.error(
              'Error getting community posts: ' + JSON.stringify(r)
            );
          },
        });
    } else {
      // TODO: redirect to error page
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
              console.error('Error getting logged user: ' + JSON.stringify(r));
            },
          });
        } else {
          // if user is not logged in
          this.loggedUsername = ''; // set the logged username to empty
          this.user = undefined;
          this.isAdmin = false;
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

  hasBanner(communityID: number | undefined): boolean {
    if (!communityID) {
      return false;
    } else {
      return this.community?.hasBanner ?? false;
    }
  }

  // Get user profile picture
  profilePicture(username: string) {
    return this.profileService.getPostImageURL(username);
  }

  openAlertModal(text: string, action: () => void) {
    this.alertModalText = text;
    this.confirmAction = action;
    this.showAlertModal = true;
  }

  closeAlertModal() {
    this.showAlertModal = false;
  }

  changeRepliesOrder(order: string, orderText: string) {
    this.postOrder = order;
    this.selectedOrderText = orderText;
    this.page = 0;
    this.posts = [];
    this.loadPosts();
  }

  searchPosts() {}

  setSearchCriteria(criteria: string) {
    this.searchCriteria = criteria;
    if (criteria === 'title') {
      this.searchCriteriaText = 'Título';
    } else if (criteria === 'content') {
      this.searchCriteriaText = 'Contenido';
    } else if (criteria === 'author') {
      this.searchCriteriaText = 'Autor';
    } else if (criteria === 'default') {
      this.searchCriteriaText = 'Título + contenido';
    }
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

  getMembers() {
    if (this.community) {
      this.communityService.getMembers(this.community.identifier, 0, 10).subscribe({
        next: (members) => {
          this.communityMembers = members;
        },
        error: (r) => {
          console.error('Error getting community members: ' + JSON.stringify(r));
        },
      });
    }
  }

  getMembersCount() {
    if (this.community) {
      this.communityService.getMembersCount(this.community.identifier).subscribe({
        next: (count) => {
          this.communityMembersCount = count;
        },
        error: (r) => {
          console.error('Error getting community members count: ' + JSON.stringify(r));
        },
      });
    }
  }

  getPostsCount() {
    if (this.community) {
      this.communityService.getPostsCount(this.community.identifier).subscribe({
        next: (count) => {
          this.communityPostsCount = count;
        },
        error: (r) => {
          console.error('Error getting community posts count: ' + JSON.stringify(r));
        },
      });
    }
  }
}
