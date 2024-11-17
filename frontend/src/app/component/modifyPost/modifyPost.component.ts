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
  selector: 'app-modifyPost',
  templateUrl: './modifyPost.component.html',
  styleUrls: ['./modifyPost.component.css', '../../../animations.css'],
  providers: [DatePipe],
})
export class ModifyPostComponent implements OnInit {
  showModal: boolean = false;

  post: Post | undefined;
  community: Community | undefined;
  postTitle: string = '';
  postContent: string = '';
  selectedImageURL: string | ArrayBuffer | null = null;

  title = 'Bookmarks Forums - Edit post';
  userLoaded = false;

  user: User | undefined;
  loggedUsername: string = '';
  loggedIn: boolean = false;
  isAdmin: boolean = false;

  public chart: any;

  // Char limits
  readonly titleMaxLength = 100;
  readonly contentMaxLength = 5000;

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

    // Add event listeners for selection change and input events
    document.addEventListener(
      'selectionchange',
      this.updateToolbarButtons.bind(this)
    );
    const postContentElement = document.getElementById('postContent');
    if (postContentElement) {
      postContentElement.addEventListener(
        'input',
        this.updateToolbarButtons.bind(this)
      );
    }
  }

  loadUserData(user: User) {
    this.user = user;
    this.loggedUsername = user.username;
    this.isAdmin = user.roles.includes('ADMIN');
  }

  loadCommunity() {
    if (this.post) {
      this.communityService
        .getCommunityById(this.post.community.identifier)
        .subscribe({
          next: (community) => {
            this.community = community;
          },
          error: (r) => {
            console.error('Error getting community: ' + JSON.stringify(r));
          },
        });
    }
  }

  loadPost() {
    let postID = Number(this.route.snapshot.paramMap.get('identifier'));
    this.postService.getPostById(postID).subscribe({
      next: (post) => {
        this.post = post;
        this.postTitle = post.title;
        this.postContent = post.content; // Set the postContent property
        this.loadCommunity();
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
    } else {
      return this.community?.hasBanner ?? false;
    }
  }

  deletePost(postId: number | undefined): void {
    if (!postId) {
      console.error('Post ID is undefined');
      return;
    }
    this.postService.deletePost(postId).subscribe({
      next: () => {
        // TODO: confirm deletion and redirect to home page or community page
      },
      error: (r) => {
        console.error('Error deleting post: ' + JSON.stringify(r));
      },
    });
  }

  editPost(postId: number | undefined): void {
    if (!postId) {
      console.error('Post ID is undefined');
      return;
    }
    // TODO: redirect to edit post page
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

  confirmEditPost(postID: number | undefined): void {
    if (!postID) {
      console.error('Post ID is undefined');
      return;
    }
    this.postTitle = (
      document.getElementById('postTitle') as HTMLInputElement
    ).value;
    const postContentElement = document.getElementById(
      'postContent'
    ) as HTMLDivElement;
    this.postContent = postContentElement.innerHTML.trim(); // Use innerHTML to get the content of the div

    if (!this.postContent) {
      console.error('Post content is empty');
      return;
    }

    //Length check
    if (this.postTitle.length > this.titleMaxLength) {
      alert(
        'Title is too long. Maximum length is ' +
          this.titleMaxLength +
          ' characters.'
      );
      return;
    } else if (this.postContent.length > this.contentMaxLength) {
      alert(
        'Content is too long. Maximum length is ' +
          this.contentMaxLength +
          ' characters.'
      );
      return;
    }

    // Trim the post title and content in case they exceed the maximum length
    this.postTitle = this.postTitle.substring(0, this.titleMaxLength);
    this.postContent = this.postContent.substring(0, this.contentMaxLength);

    const postDTO = new FormData();
    postDTO.append('title', this.postTitle);
    postDTO.append('content', this.postContent);

    // Upload the image if selected
    if (this.selectedImageURL) {
      const input = document.querySelector(
        'input[type="file"]'
      ) as HTMLInputElement;
      if (input.files && input.files.length > 0) {
        const file = input.files[0];
        postDTO.append('image', file);
      }
    }

    console.log('Post DTO title: ' + postDTO.get('title'));
    console.log('Post DTO content: ' + postDTO.get('content'));

    this.postService.editPost(postID, postDTO, 'edit').subscribe({
      next: () => {
        // Redirect to the post page
        window.location.href = '/post/' + postID;
      },
      error: (r) => {
        console.error('Error editing post: ' + JSON.stringify(r));
      },
    });
  }

  cancelEditPost(): void {
    //redirect to post page
    window.location.href = '/post/' + this.post?.identifier;
  }

  handleKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      this.openModal();
    } else if (event.key === 'Escape') {
      this.closeModal();
    }
  }

  adjustTextareaHeight(event: Event): void {
    const textarea = event.target as HTMLTextAreaElement;
    const scrollTop = window.scrollY || document.documentElement.scrollTop;
    textarea.style.height = 'auto'; // Reset the height
    textarea.style.height = `${textarea.scrollHeight}px`; // Set the height to match the content
    window.scrollTo(0, scrollTop); // Restore the scroll position
  }

  deletePostImage(postID: number | undefined): void {
    if (!postID) {
      console.error('Post ID is undefined');
      return;
    }
    // show confirmation dialog
    if (
      confirm(
        'Are you sure you want to delete the image? This action cannot be undone.'
      )
    ) {
      this.postService.deletePostImage(postID).subscribe({
        next: (response: string) => {
          console.log(response); // Log the response string
          if (this.post) {
            this.post.hasImage = false; // Update the post object to reflect the image deletion
          }
          console.log('Image deleted successfully');
        },
        error: (r) => {
          console.error('Error deleting image: ' + JSON.stringify(r));
        },
      });
    }
  }

  uploadPostImage(postID: number | undefined, file: File): void {
    if (!postID) {
      console.error('Post ID is undefined');
      return;
    }
    this.postService.updatePostImage(postID, file).subscribe({
      next: (post) => {
        this.post = post;
        console.log('Image uploaded successfully');
      },
      error: (r) => {
        console.error('Error uploading image: ' + JSON.stringify(r));
      },
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      const reader = new FileReader();
      reader.onload = () => {
        this.selectedImageURL = reader.result; // Set the selected image URL
      };
      reader.readAsDataURL(file);
    }
  }

  applyStyle(style: string, event: Event): void {
    event.preventDefault(); // Prevent default button behavior
    document.execCommand(style, false, '');
    this.updateToolbarButtons();
    this.refocusPostContent(); // Refocus the postContent element
  }

  applyTextList(style: string, event: Event): void {
    event.preventDefault(); // Prevent default button behavior
    if (style === 'unorderedList') {
      document.execCommand('insertUnorderedList', false, '');
    } else if (style === 'orderedList') {
      document.execCommand('insertOrderedList', false, '');
    }
    this.updateToolbarButtons();
    this.refocusPostContent(); // Refocus the postContent element
  }

  refocusPostContent(): void {
    const postContentElement = document.getElementById('postContent');
    if (postContentElement) {
      postContentElement.focus();
    }
  }

  addLink(): void {
    const url = prompt('Enter the URL');
    if (url) {
      const selection = window.getSelection();
      if (selection && selection.rangeCount > 0) {
        const range = selection.getRangeAt(0);
        const selectedText = range.toString() || url;
        const anchor = document.createElement('a');
        anchor.href = url;
        anchor.textContent = selectedText;
        range.deleteContents();
        range.insertNode(anchor);
      }
    }
  }

  updateToolbarButtons(): void {
    const boldButton = document.querySelector('.toolbar-btn[title="Negrita"]');
    const italicButton = document.querySelector(
      '.toolbar-btn[title="Cursiva"]'
    );
    const underlineButton = document.querySelector(
      '.toolbar-btn[title="Subrayado"]'
    );
    const strikethroughButton = document.querySelector(
      '.toolbar-btn[title="Tachado"]'
    );

    if (
      !boldButton ||
      !italicButton ||
      !underlineButton ||
      !strikethroughButton
    ) {
      return;
    }

    const selection = window.getSelection();
    if (!selection || selection.rangeCount === 0) {
      return;
    }

    const range = selection.getRangeAt(0);
    const parentElement = range.commonAncestorContainer.parentElement;

    if (!parentElement) {
      return;
    }

    boldButton.classList.toggle('active', document.queryCommandState('bold'));
    italicButton.classList.toggle(
      'active',
      document.queryCommandState('italic')
    );
    underlineButton.classList.toggle(
      'active',
      document.queryCommandState('underline')
    );
    strikethroughButton.classList.toggle(
      'active',
      document.queryCommandState('strikethrough')
    );
  }

  checkContentLength(): void {
    const postContentElement = document.getElementById(
      'postContent'
    ) as HTMLDivElement;
    const postTitleElement = document.getElementById(
      'postTitle'
    ) as HTMLInputElement;

    // Check and trim post content
    if (postContentElement.innerText.length > this.contentMaxLength) {
      postContentElement.innerText = postContentElement.innerText.substring(
        0,
        this.contentMaxLength
      );
    }

    // Check and trim post title
    if (postTitleElement.value.length > this.titleMaxLength) {
      postTitleElement.value = postTitleElement.value.substring(
        0,
        this.titleMaxLength
      );
    }

    // Add or remove red border for content
    if (postContentElement.innerText.length > this.contentMaxLength * 0.9) {
      postContentElement.classList.add('red-border');
    } else {
      postContentElement.classList.remove('red-border');
    }

    // Add or remove red underline for title
    if (postTitleElement.value.length > this.titleMaxLength * 0.9) {
      postTitleElement.classList.add('red-underline');
    } else {
      postTitleElement.classList.remove('red-underline');
    }
  }

  // Prevent further input when content limit is reached
  preventExceedingContent(event: KeyboardEvent): void {
    const postContentElement = document.getElementById(
      'postContent'
    ) as HTMLDivElement;
    const postTitleElement = document.getElementById(
      'postTitle'
    ) as HTMLInputElement;

    const isTyping =
      !event.ctrlKey &&
      !event.metaKey &&
      !event.altKey &&
      event.key.length === 1;

    if (isTyping) {
      if (
        postContentElement.innerText.length >= this.contentMaxLength &&
        event.target === postContentElement
      ) {
        event.preventDefault();
      }

      if (
        postTitleElement.value.length >= this.titleMaxLength &&
        event.target === postTitleElement
      ) {
        event.preventDefault();
      }
    }
  }
}
