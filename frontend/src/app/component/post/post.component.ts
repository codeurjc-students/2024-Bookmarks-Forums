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
  post: Post | undefined;

  replies: Reply[] = [];
  replyTitle: string = '';
  replyContent: string = '';
  replyTitleError: string = '';
  replyContentError: string = '';

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
    this.loadPost();
  }

  loadUserData(user: User) {
    this.user = user;
    this.loggedUsername = user.username;
    this.isAdmin = user.roles.includes('ADMIN');
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

  loadPost() {
    let postID = Number(this.route.snapshot.paramMap.get('identifier'));
    this.postService.getPostById(Number(postID)).subscribe({
      next: (post) => {
        this.post = post;
        this.loadReplies();
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
    }
    return this.communityService.getCommunityImageURL(communityID) !== '';
  }

  deleteReply(replyID: number) {
    this.postService.deleteReply(replyID).subscribe({
      next: () => {
        this.replies = this.replies.filter(
          (reply) => reply.identifier !== replyID
        );
      },
      error: (r) => {
        console.error('Error deleting reply: ' + JSON.stringify(r));
      },
    });
  }
}
