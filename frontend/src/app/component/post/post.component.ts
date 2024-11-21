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
import { Reply } from '../../models/reply.model';

Chart.register(...registerables);

@Component({
  selector: 'app-post',
  templateUrl: './post.component.html',
  styleUrls: ['./post.component.css', '../../../animations.css'],
  providers: [DatePipe],
})
export class PostComponent implements OnInit {
  showModal: boolean = false;

  selectedOrderText: string = 'Fecha de creación'; // Default text

  showAdvancedMenu: boolean = false;

  searchTerm: string = '';
  searchCriteria: string = 'default'; // Default search criteria (can be title, content or author)
  searchCriteriaText: string = 'Título + Contenido'; // Default search criteria text
  post: Post | undefined;
  community: Community | undefined;

  upvotes: number = 0;
  downvotes: number = 0;

  upvoted: boolean = false;
  downvoted: boolean = false;

  replies: Reply[] = [];
  replyTitle: string = '';
  replyContent: string = '';
  replyTitleError: string = '';
  replyContentError: string = '';
  postReplies: number = 0;

  // ReplyID and boolean to check if user liked the reply
  likedReplyList: Map<number, boolean> = new Map<number, boolean>();

  title = 'Bookmarks';
  userLoaded = false;

  user: User | undefined;
  loggedUsername: string = '';
  loggedIn: boolean = false;
  isAdmin: boolean = false;

  public chart: any;

  // replies pagination
  size = 4;
  page = 0;
  repliesOrder = 'creationDate'; // order by creation date (most recent first); can be changed to "rating" to order by rating

  loadingMoreReplies = false;

  noMoreReplies = false;

  showAlertModal: boolean = false;
  alertModalText: string = '';
  confirmAction: () => void = () => {};

  isUserBanned: boolean = false;

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
  }

  loadUserData(user: User) {
    this.user = user;
    this.loggedUsername = user.username;
    this.isAdmin = user.roles.includes('ADMIN');
    this.loadUserPostVotes();
    this.loadLikedReplies();
  }

  submitReply(postId: number | undefined): void {
    const titleMaxLength = 150;
    const contentMaxLength = 500;

    const titleInput = document.querySelector('.reply-title-input');
    const contentInput = document.querySelector('.reply-content-input');

    let isValid = true;

    if (!this.replyTitle.trim()) {
      this.replyTitleError = 'El título no puede estar vacío.';
      if (titleInput) {
        titleInput.classList.add('is-invalid');
      }
      isValid = false;
    } else if (this.replyTitle.length > titleMaxLength) {
      this.replyTitleError = `El título no puede tener más de ${titleMaxLength} caracteres.`;
      if (titleInput) {
        titleInput.classList.add('is-invalid');
      }
      isValid = false;
    } else {
      this.replyTitleError = '';
      if (titleInput) {
        titleInput.classList.remove('is-invalid');
      }
    }

    if (!this.replyContent.trim()) {
      this.replyContentError = 'El contenido no puede estar vacío.';
      if (contentInput) {
        contentInput.classList.add('is-invalid');
      }
      isValid = false;
    } else if (this.replyContent.length > contentMaxLength) {
      this.replyContentError = `El contenido no puede tener más de ${contentMaxLength} caracteres.`;
      if (contentInput) {
        contentInput.classList.add('is-invalid');
      }
      isValid = false;
    } else {
      this.replyContentError = '';
      if (contentInput) {
        contentInput.classList.remove('is-invalid');
      }
    }

    if (!isValid) {
      return;
    }

    const replyData = {
      title: this.replyTitle,
      content: this.replyContent,
    };

    if (!postId) {
      //TODO: redirect to error page
      console.error('Post ID is undefined');
      return;
    }

    this.postService.createReply(postId, replyData).subscribe({
      next: (reply) => {
        this.replies.push(reply);
        this.postReplies += 1;
        this.replyTitle = ''; // Clear the title field
        this.replyContent = ''; // Clear the content field

        // Remove invalid class from input fields
        if (titleInput) {
          titleInput.classList.remove('is-invalid');
        }
        if (contentInput) {
          contentInput.classList.remove('is-invalid');
        }
      },
      error: (r) => {
        console.error('Error creating reply: ' + JSON.stringify(r));
      },
    });
  }

  loadLikedReplies() {
    if (!this.user) {
      return;
    }
    this.replies.forEach((reply) => {
      this.postService
        .hasUserLikedReply(reply.identifier, this.loggedUsername)
        .subscribe({
          next: (liked) => {
            this.likedReplyList.set(reply.identifier, liked);
          },
          error: (r) => {
            console.error(
              'Error checking if user liked reply: ' + JSON.stringify(r)
            );
          },
        });
    });
  }

  loadReplies() {
    if (this.post) {
      this.postService
        .getRepliesOfPost(
          this.post.identifier,
          this.page,
          this.size,
          this.repliesOrder
        )
        .subscribe({
          next: (replies) => {
            if (!replies || replies.length == 0) {
              this.noMoreReplies = true;
              this.loadingMoreReplies = false;
              return;
            }
            this.replies = this.replies.concat(replies);
            this.loadingMoreReplies = false;
            this.page += 1;
            this.loadLikedReplies();

            if (replies.length < this.size) {
              this.noMoreReplies = true;
            }
          },
          error: (r) => {
            console.error('Error getting replies: ' + JSON.stringify(r));
          },
        });
    } else {
      // TODO: redirect to error page
      console.error('Post is undefined');
    }
  }

  loadMoreReplies() {
    this.loadingMoreReplies = true;
    this.loadReplies();
    this.loadingMoreReplies = false;
  }

  loadUserBanStatus() {
    if (this.user && this.community) {
      this.communityService
        .isUserBanned(this.community.identifier, this.user.username)
        .subscribe({
          next: (banID) => {
            if (banID != -1) {
              this.isUserBanned = true;
            } else {
              this.isUserBanned = false;
            }
          },
          error: (r) => {
            console.error(
              'Error checking if user is banned: ' + JSON.stringify(r)
            );
          },
        });
    }
  }

  loadCommunity() {
    if (this.post) {
      this.communityService
        .getCommunityById(this.post.community.identifier)
        .subscribe({
          next: (community) => {
            this.community = community;
            this.loadUserBanStatus();
          },
          error: (r) => {
            console.error('Error getting community: ' + JSON.stringify(r));
          },
        });
    }
  }

  loadUserPostVotes() {
    if (this.post) {
      this.postService
        .hasUserVoted(this.post.identifier, this.loggedUsername, 'upvote')
        .subscribe({
          next: (voted) => {
            this.upvoted = voted;
          },
          error: (r) => {
            console.error(
              'Error checking if user upvoted post: ' + JSON.stringify(r)
            );
          },
        });

      this.postService
        .hasUserVoted(this.post.identifier, this.loggedUsername, 'downvote')
        .subscribe({
          next: (voted) => {
            this.downvoted = voted;
          },
          error: (r) => {
            console.error(
              'Error checking if user downvoted post: ' + JSON.stringify(r)
            );
          },
        });
    }
  }

  loadPostVotes() {
    if (this.post) {
      this.upvotes = this.post.upvotes;
      this.downvotes = this.post.downvotes;
    }
  }

  loadPost() {
    let postID = Number(this.route.snapshot.paramMap.get('identifier'));
    this.postService.getPostById(Number(postID)).subscribe({
      next: (post) => {
        this.post = post;
        this.postReplies = post.comments;
        this.loadReplies();
        this.loadCommunity();
        this.loadPostVotes();
      },
      error: (r) => {
        console.error('Error getting post: ' + JSON.stringify(r));
      },
    });
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
              this.loadPost();
            },
            error: (r) => {
              console.error('Error getting logged user: ' + JSON.stringify(r));
              this.loadPost();
            },
          });
        } else {
          // if user is not logged in
          this.loggedUsername = ''; // set the logged username to empty
          this.user = undefined;
          this.isAdmin = false;
          this.loadPost();
        }
      },
      error: (r) => {
        // if error is 401, user is not logged in, do not print error
        if (r.status != 401) {
          console.error(
            'Error checking if user is logged in: ' + JSON.stringify(r)
          );
        }
        this.loadPost();
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
  profilePicture(username: string | undefined) {
    if (!username) {
      return;
    }
    return this.profileService.getPostImageURL(username);
  }

  hasBanner(communityID: number | undefined): boolean {
    if (!communityID) {
      return false;
    } else {
      return this.community?.hasBanner ?? false;
    }
  }

  deleteReply(replyID: number) {
    this.postService.deleteReply(replyID).subscribe({
      next: () => {
        this.replies = this.replies.filter(
          (reply) => reply.identifier !== replyID
        );
        this.likedReplyList.delete(replyID);
        this.postReplies -= 1;
      },
      error: (r) => {
        console.error('Error deleting reply: ' + JSON.stringify(r));
      },
    });
  }

  openAlertModal(text: string, action: () => void) {
    this.alertModalText = text;
    this.confirmAction = action;
    this.showAlertModal = true;
  }

  closeAlertModal() {
    this.showAlertModal = false;
  }

  deletePost(postId: number | undefined): void {
    if (!postId) {
      console.error('Post ID is undefined');
      return;
    }
    this.openAlertModal(
      '¿Seguro que quieres eliminar este post? Esta acción no se puede deshacer.',
      () => {
        this.postService.deletePost(postId).subscribe({
          next: (response: string) => {
            // go back to previous page
            // if the previous page is the post page, goes to the home page
            if (document.referrer.includes('post')) {
              window.location.href = '/';
            } else {
              window.history.back();
            }
          },
          error: (r) => {
            console.error('Error deleting post: ' + JSON.stringify(r));
          },
        });
      }
    );
  }

  editPost(postId: number | undefined): void {
    if (!postId) {
      console.error('Post ID is undefined');
      return;
    }
    // Redirects to post/:identifier/edit
    window.location.href = `/post/${postId}/edit`;
  }

  downvotePost(postId: number | undefined): void {
    if (!postId) {
      console.error('Post ID is undefined');
      return;
    }
    this.postService.editPost(postId, new FormData(), 'downvote').subscribe({
      next: () => {
        if (this.upvoted) {
          // If the post was upvoted, remove the upvote and add a downvote
          this.upvotes -= 1;
          this.upvoted = false;
          this.downvotes += 1;
          this.downvoted = true;
        } else if (this.downvoted) {
          // If the post was already downvoted, remove the downvote
          this.downvotes -= 1;
          this.downvoted = false;
        } else {
          // If the post was not voted, add a downvote
          this.downvotes += 1;
          this.downvoted = true;
        }
      },
      error: (r) => {
        console.error('Error downvoting post: ' + JSON.stringify(r));
      },
    });
  }

  upvotePost(postId: number | undefined): void {
    if (!postId) {
      console.error('Post ID is undefined');
      return;
    }
    this.postService.editPost(postId, new FormData(), 'upvote').subscribe({
      next: () => {
        if (this.downvoted) {
          // If the post was downvoted, remove the downvote and add an upvote
          this.downvotes -= 1;
          this.downvoted = false;
          this.upvotes += 1;
          this.upvoted = true;
        } else if (this.upvoted) {
          // If the post was already upvoted, remove the upvote
          this.upvotes -= 1;
          this.upvoted = false;
        } else {
          // If the post was not voted, add an upvote
          this.upvotes += 1;
          this.upvoted = true;
        }
      },
      error: (r) => {
        console.error('Error upvoting post: ' + JSON.stringify(r));
      },
    });
  }

  hasLikedReply(replyID: number) {
    return this.likedReplyList.get(replyID);
  }

  upvoteReply(replyID: number) {
    if (!this.user) {
      return;
    }
    this.postService.likeReply(replyID, 'like').subscribe({
      next: () => {
        this.replies = this.replies.map((reply) => {
          if (reply.identifier === replyID) {
            if (this.likedReplyList.get(replyID)) {
              reply.likes -= 1;
              this.likedReplyList.set(replyID, false);
            } else {
              reply.likes += 1;
              this.likedReplyList.set(replyID, true);
            }
          }
          return reply;
        });
      },
      error: (r) => {
        console.error('Error upvoting reply: ' + JSON.stringify(r));
      },
    });
  }

  changeRepliesOrder(order: string, orderText: string) {
    this.repliesOrder = order;
    this.selectedOrderText = orderText;
    this.page = 0;
    this.replies = [];
    this.loadReplies();
  }

  searchReplies(postId: number | undefined) {
    if (!postId) {
      console.error('Post ID is undefined');
      return;
    }
    if (this.post) {
      this.page = 0;
      this.replies = [];
      this.postService
        .searchReplies(
          this.post.identifier,
          this.searchCriteria,
          this.searchTerm,
          this.page,
          this.size
        )
        .subscribe({
          next: (replies) => {
            this.replies = this.replies.concat(replies);
            this.loadLikedReplies();
            this.page += 1;
          },
          error: (r) => {
            console.error('Error searching replies: ' + JSON.stringify(r));
          },
        });
    }
  }

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
}
