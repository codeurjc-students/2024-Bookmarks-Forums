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
import { Reply } from '../../models/reply.model';
import { Router } from '@angular/router';
import { TitleService } from '../../services/title.service';


Chart.register(...registerables);

@Component({
  selector: 'app-post',
  templateUrl: './post.component.html',
  styleUrls: ['./post.component.css', '../../../animations.css'],
  providers: [DatePipe],
})
export class PostComponent implements OnInit, OnDestroy {
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

  // Add tracking for disabled states
  postVotingDisabled: boolean = false;
  disabledReplyVotes: Set<number> = new Set<number>();

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

  isCommunityAdmin: boolean = false;
  isModerator: boolean = false;

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
    this.titleService.setTitle('Post');
    // Check if user is logged in
    this.checkIfLoggedIn();
  }

  loadUserData(user: User | undefined) {
    if (!user) {
      return;
    }
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
      this.router.navigate(['/error'], {
        queryParams: {
          title: 'Error al crear respuesta',
          description: 'No se ha encontrado el post al que responder.',
          code: 404,
        },
      });
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
        this.router.navigate(['/error'], {
          queryParams: {
            title: 'Error al crear respuesta',
            description: r.error.message,
            code: r.status,
          },
        });
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
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Error al cargar respuestas',
                description: r.error.message,
                code: r.status,
              },
            });
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
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Error al cargar respuestas',
                description: r.error.message,
                code: r.status,
              },
            });
          },
        });
    } else {
      this.router.navigate(['/error'], {
        queryParams: {
          title: 'Error al cargar respuestas',
          description: 'No se ha encontrado el post al que responder.',
          code: 404,
        },
      });
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
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Error al cargar estado de baneo',
                description: r.error.message,
                code: r.status,
              },
            });
          },
        });
    }
  }

  checkCommunityAdmin(){
    if(this.community && this.user){
      this.isCommunityAdmin = this.community.admin.username === this.loggedUsername;
    }
  }

  checkModerator() {
    if (this.user && this.community) {
      this.communityService
        .isModerator(this.community.identifier, this.user.username)
        .subscribe({
          next: (moderator) => {
            this.isModerator = moderator;
          },
          error: (r) => {
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Error al cargar moderador',
                description: r.error.message,
                code: r.status,
              },
            });
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
            this.checkModerator();
            this.checkCommunityAdmin();
          },
          error: (r) => {
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Error al cargar comunidad',
                description: r.error.message,
                code: r.status,
              },
            });
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
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Error al cargar votos de post',
                description: r.error.message,
                code: r.status,
              },
            });
          },
        });

      this.postService
        .hasUserVoted(this.post.identifier, this.loggedUsername, 'downvote')
        .subscribe({
          next: (voted) => {
            this.downvoted = voted;
          },
          error: (r) => {
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Error al cargar votos de post',
                description: r.error.message,
                code: r.status,
              },
            });
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
        this.titleService.setTitle(`${post.title}`);
        this.post = post;
        this.postReplies = post.comments;
        this.loadUserData(this.user); // load the user data
        this.loadReplies();
        this.loadCommunity();
        this.loadPostVotes();
      },
      error: (r) => {
        // if error is 404, post does not exist
        if (r.status == 404) {
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error al cargar post',
              description: 'No se ha encontrado el post.',
              code: '404',
            },
          });
        } else {
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error al cargar post',
              description: r.error.message,
              code: r.status,
            },
          });
        }
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
              this.user = user;
              this.loggedUsername = user.username;
              this.isAdmin = user.roles.includes('ADMIN');
              this.loadPost();
            },
            error: (r) => {
              this.router.navigate(['/error'], {
                queryParams: {
                  title: 'Error al cargar usuario',
                  description: r.error.message,
                  code: r.status,
                },
              });
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
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error al comprobar si está logueado',
              description: r.error.message,
              code: r.status,
            },
          });
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
        this.router.navigate(['/error'], {
          queryParams: {
            title: 'Error al eliminar respuesta',
            description: r.error.message,
            code: r.status,
          },
        });
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
      this.router.navigate(['/error'], {
        queryParams: {
          title: 'Error al eliminar post',
          description: 'No se ha encontrado el post a eliminar.',
          code: 404,
        },
      });
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
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Error al eliminar post',
                description: r.error.message,
                code: r.status,
              },
            });
          },
        });
      }
    );
  }

  editPost(postId: number | undefined): void {
    if (!postId) {
      this.router.navigate(['/error'], {
        queryParams: {
          title: 'Error al editar post',
          description: 'No se ha encontrado el post a editar.',
          code: 404,
        },
      });
      return;
    }
    // Redirects to post/:identifier/edit
    window.location.href = `/post/${postId}/edit`;
  }

  downvotePost(postId: number | undefined): void {
    if (!postId || this.postVotingDisabled) {
      return;
    }
    
    // Disable both voting buttons while processing
    this.postVotingDisabled = true;
    
    this.postService.editPost(postId, new FormData(), 'downvote').subscribe({
      next: () => {
        if (this.upvoted) {
          this.upvotes -= 1;
          this.upvoted = false;
          this.downvotes += 1;
          this.downvoted = true;
        } else if (this.downvoted) {
          this.downvotes -= 1;
          this.downvoted = false;
        } else {
          this.downvotes += 1;
          this.downvoted = true;
        }
        // Re-enable voting buttons after successful update
        this.postVotingDisabled = false;
      },
      error: (r) => {
        // Re-enable voting buttons on error
        this.postVotingDisabled = false;
        this.router.navigate(['/error'], {
          queryParams: {
            title: 'Error al votar post',
            description: r.error.message,
            code: r.status,
          },
        });
      },
    });
  }

  upvotePost(postId: number | undefined): void {
    if (!postId || this.postVotingDisabled) {
      return;
    }
    
    // Disable both voting buttons while processing
    this.postVotingDisabled = true;
    
    this.postService.editPost(postId, new FormData(), 'upvote').subscribe({
      next: () => {
        if (this.downvoted) {
          this.downvotes -= 1;
          this.downvoted = false;
          this.upvotes += 1;
          this.upvoted = true;
        } else if (this.upvoted) {
          this.upvotes -= 1;
          this.upvoted = false;
        } else {
          this.upvotes += 1;
          this.upvoted = true;
        }
        // Re-enable voting buttons after successful update
        this.postVotingDisabled = false;
      },
      error: (r) => {
        // Re-enable voting buttons on error
        this.postVotingDisabled = false;
        this.router.navigate(['/error'], {
          queryParams: {
            title: 'Error al votar post',
            description: r.error.message,
            code: r.status,
          },
        });
      },
    });
  }

  hasLikedReply(replyID: number) {
    return this.likedReplyList.get(replyID);
  }

  upvoteReply(replyID: number) {
    if (!this.user || this.disabledReplyVotes.has(replyID)) {
      return;
    }

    // Disable the reply's vote button while processing
    this.disabledReplyVotes.add(replyID);
    
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
        // Re-enable the reply's vote button after successful update
        this.disabledReplyVotes.delete(replyID);
      },
      error: (r) => {
        // Re-enable the reply's vote button on error
        this.disabledReplyVotes.delete(replyID);
        this.router.navigate(['/error'], {
          queryParams: {
            title: 'Error al votar respuesta',
            description: r.error.message,
            code: r.status,
          },
        });
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
      this.router.navigate(['/error'], {
        queryParams: {
          title: 'Error al buscar respuestas',
          description: 'No se ha encontrado el post al que responder.',
          code: 404,
        },
      });
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

  handleLinkClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (target.tagName === 'A') {
      event.preventDefault();
      const href = target.getAttribute('href');
      if (href && !href.startsWith('/')) {
        // It's an external link
        this.openAlertModal(
          `¿Estás seguro de que quieres visitar ${href}?`,
          () => {
            window.open(href, '_blank', 'noopener,noreferrer');
          }
        );
      } else {
        // It's an internal link, navigate normally
        window.location.href = href || '/';
      }
    }
  }

  // Column toggle methods
  setActiveColumn(column: 'main' | 'side') {
    this.activeColumn = column;
  }

  private checkIfMobile() {
    this.isMobile = window.innerWidth < 992; // 992px is Bootstrap's lg breakpoint
  }

  ngOnDestroy() {
    // Remove resize listener when component is destroyed
    window.removeEventListener('resize', () => this.checkIfMobile());
  }
}
