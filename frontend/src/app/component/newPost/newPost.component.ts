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
import { Router } from '@angular/router';

Chart.register(...registerables);

@Component({
  selector: 'app-modifyPost',
  templateUrl: './newPost.component.html',
  styleUrls: ['./newPost.component.css', '../../../animations.css'],
  providers: [DatePipe],
})
export class NewPostComponent implements OnInit {
  showModal: boolean = false;
  showAlertModal: boolean = false;
  alertModalText: string = '';
  confirmAction: () => void = () => {};
  showCancelButton: boolean = true;

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
  isMember: boolean = false;

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
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Check if user is logged in
    this.checkIfLoggedIn();

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
    this.loadCommunity();
  }

  loadIsMember() {
    if (this.community && this.user) {
      this.communityService
        .isUserMember(this.community.identifier, this.user.username)
        .subscribe({
          next: (isMember) => {
            this.isMember = isMember;
            // if user is not a member or an admin, redirect to error page
            if (!this.isMember) {
              this.router.navigate(['/error'], {
                queryParams: {
                  title: 'Error al cargar la comunidad',
                  description: 'No tienes permiso para acceder a esta página.',
                  code: 403,
                },
              });
            }
          },
          error: (r) => {
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Error al cargar la comunidad',
                description: r.error.message,
                code: r.status,
              },
            });
          },
        });
    }
  }

  loadCommunity() {
    let communityID = Number(this.route.snapshot.paramMap.get('identifier'));
    this.communityService.getCommunityById(communityID).subscribe({
      next: (community) => {
        this.community = community;
        this.loadIsMember();
      },
      error: (r) => {
        this.router.navigate(['/error'], {
          queryParams: {
            title: 'Error al cargar la comunidad',
            description: r.error.message,
            code: r.status,
          },
        });
      },
    });
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
                  title: 'Error al cargar el usuario',
                  description: r.error.message,
                  code: r.status,
                },
              });
            },
          });
        } else {
          // if user is not logged in
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error al cargar el usuario',
              description: 'Debes iniciar sesión para acceder a esta página.',
              code: 401,
            },
          });
        }
      },
      error: (r) => {
        this.router.navigate(['/error'], {
          queryParams: {
            title: 'Error al cargar el usuario',
            description: r.error.message,
            code: r.status,
          },
        });
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
      this.router.navigate(['/error'], {
        queryParams: {
          title: 'Error al eliminar el post',
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
                title: 'Error al eliminar el post',
                description: r.error.message,
                code: r.status,
              },
            });
          },
        });
      },
      true
    );
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

  openAlertModal(text: string, action: () => void, showCancel: boolean) {
    this.alertModalText = text;
    this.confirmAction = action;
    this.showAlertModal = true;
    this.showCancelButton = showCancel;
  }

  closeAlertModal() {
    this.showAlertModal = false;
  }

  isContentEmpty(content: string): boolean {
    const strippedContent = content.replace(/<[^>]*>/g, '').trim();
    return strippedContent === '';
  }

  confirmEditPost(): void {
    if (!this.community) {
      this.router.navigate(['/error'], {
        queryParams: {
          title: 'Error al editar el post',
          description:
            'No se ha encontrado la comunidad a la que pertenece el post.',
          code: 404,
        },
      });
      return;
    }
    this.postTitle = (
      document.getElementById('postTitle') as HTMLInputElement
    ).value;
    const postContentElement = document.getElementById(
      'postContent'
    ) as HTMLDivElement;
    this.postContent = postContentElement.innerHTML.trim(); // Use innerHTML to get the content of the div

    if (this.isContentEmpty(this.postContent)) {
      // modal alert
      this.openAlertModal(
        'El contenido del post no puede estar vacío.',
        () => {
          this.closeAlertModal();
        },
        false
      );
      return;
    }

    if (!this.postTitle || this.postTitle.trim() === '') {
      // modal alert
      this.openAlertModal(
        'El título del post no puede estar vacío.',
        () => {
          this.closeAlertModal();
        },
        false
      );
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

    this.postService.createPost(this.community?.identifier, postDTO).subscribe({
      next: (post) => {
        //redirect to post page
        window.location.href = '/post/' + post.identifier;
      },
      error: (r) => {
        this.router.navigate(['/error'], {
          queryParams: {
            title: 'Error al crear el post',
            description: r.error.message,
            code: r.status,
          },
        });
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

  deletePostImage(): void {
    if (this.selectedImageURL) {
      this.selectedImageURL = null;
    }
  }

  uploadPostImage(postID: number | undefined, file: File): void {
    if (!postID) {
      this.router.navigate(['/error'], {
        queryParams: {
          title: 'Error al subir la imagen',
          description:
            'No se ha encontrado el post al que pertenece la imagen.',
          code: 404,
        },
      });
      return;
    }
    this.postService.updatePostImage(postID, file).subscribe({
      next: (post) => {
        this.post = post;
      },
      error: (r) => {
        this.router.navigate(['/error'], {
          queryParams: {
            title: 'Error al subir la imagen',
            description: r.error.message,
            code: r.status,
          },
        });
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
    if (!postContentElement) {
      return;
    }
    if (postContentElement.innerText.length > this.contentMaxLength) {
      postContentElement.innerText = postContentElement.innerText.substring(
        0,
        this.contentMaxLength
      );
    }

    // Check and trim post title
    if (!postTitleElement) {
      return;
    }
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
