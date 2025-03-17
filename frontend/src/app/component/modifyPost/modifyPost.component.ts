import {Component, OnInit, ViewChild, ElementRef} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {ActivatedRoute} from '@angular/router';
import {LoginService} from '../../services/session.service';
import {PostService} from '../../services/post.service';
import {UserService} from '../../services/user.service';
import {CommunityService} from '../../services/community.service';
import {User} from '../../models/user.model';
import {Post} from '../../models/post.model';
import {Community} from '../../models/community.model';
import {Chart, registerables} from 'chart.js';
import {DatePipe} from '@angular/common';
import {Router} from '@angular/router';
import { TitleService } from '../../services/title.service';
Chart.register(...registerables);

@Component({
  selector: 'app-modifyPost',
  templateUrl: './modifyPost.component.html',
  styleUrls: ['./modifyPost.component.css', '../../../animations.css'],
  providers: [DatePipe],
})
export class ModifyPostComponent implements OnInit {
  @ViewChild('toolbar') toolbar!: ElementRef;
  showModal: boolean = false;
  showAlertModal: boolean = false;
  showHyperlinkModal: boolean = false;
  hyperlinkURL: string = '';
  alertModalText: string = '';
  confirmAction: () => void = () => {
  };
  showCancelButton: boolean = true;
  savedSelection: Range | null = null;

  post: Post | undefined;
  community: Community | undefined;
  isCommunityAdmin: boolean = false;
  postTitle: string = '';
  postContent: string = '';
  selectedImageURL: string | ArrayBuffer | null = null;
  hasToDeleteImage: boolean = false;

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

  showLeftGradient: boolean = false;
  showRightGradient: boolean = false;

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
  }

  ngOnInit(): void {
    this.titleService.setTitle('Editor de post');
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

  ngAfterViewInit() {
    this.checkGradients();
  }

  onToolbarScroll() {
    this.checkGradients();
  }

  checkGradients() {
    if (this.toolbar) {
      const element = this.toolbar.nativeElement;
      this.showLeftGradient = element.scrollLeft > 0;
      this.showRightGradient = element.scrollLeft < (element.scrollWidth - element.clientWidth);
    }
  }

  loadUserData(user: User) {
    this.user = user;
    this.loggedUsername = user.username;
    this.isAdmin = user.roles.includes('ADMIN');
    this.loadPost();
  }

  loadCommunity() {
    if (this.post) {
      this.communityService
        .getCommunityById(this.post.community.identifier)
        .subscribe({
          next: (community) => {
            this.community = community;
            this.isCommunityAdmin = community.admin.username === this.loggedUsername;
            if (this.post) {
              if (
                this.post.author.username !== this.loggedUsername &&
                !this.isAdmin &&
                community.admin.username !== this.loggedUsername
              ) {
                this.router.navigate(['/error'], {
                  queryParams: {
                    title: 'Error cargando el post',
                    description:
                      'No tienes permiso para editar este post. Solo el autor o un administrador de la comunidad pueden editar un post.',
                    code: 403,
                  },
                });
              }
            }
          },
          error: (r) => {
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Error cargando la comunidad',
                description: r.error.message,
                code: 500,
              },
            });
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
        // if error is 404, post does not exist, redirect to error page
        if (r.status == 404) {
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error cargando el post',
              description: 'El post que intentas editar no existe.',
              code: 404,
            },
          });
        } else {
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error cargando el post',
              description: r.error.message,
              code: 500,
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
            title: 'Error cargando el post',
            description: r.error.message,
            code: 500,
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
              title: 'Error cargando la imagen del post',
              description: r.error.message,
              code: 500,
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
                  title: 'Error cargando el usuario',
                  description: r.error.message,
                  code: 500,
                },
              });
            },
          });
        } else {
          // if user is not logged in redirect to error page
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error cargando la sesión',
              description: 'No se ha podido cargar la sesión del usuario.',
              code: 500,
            },
          });
        }
      },
      error: (r) => {
        // if error is 401, user is not logged in, do not print error
        if (r.status != 401) {
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error cargando la sesión',
              description: r.error.message,
              code: 500,
            },
          });
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
      this.router.navigate(['/error'], {
        queryParams: {
          title: 'Error eliminando el post',
          description:
            'No se ha podido eliminar el post porque no se ha encontrado el identificador del post.',
          code: 500,
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
                title: 'Error eliminando el post',
                description: r.error.message,
                code: 500,
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

  openSingleAlertModal(text: string, action: () => void) {
    this.alertModalText = text;
    let cancelButton = document.getElementById('cancelButton');
    if (cancelButton) {
      cancelButton.style.display = 'none';
    }
    this.confirmAction = action;
    this.showAlertModal = true;
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

  confirmEditPost(postID: number | undefined): void {
    if (!postID) {
      this.router.navigate(['/error'], {
        queryParams: {
          title: 'Error editando el post',
          description:
            'No se ha podido editar el post porque no se ha encontrado el identificador del post.',
          code: 500,
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

    this.postService.editPost(postID, postDTO, 'edit').subscribe({
      next: () => {
        this.managePostImage();
      },
      error: (r) => {
        this.router.navigate(['/error'], {
          queryParams: {
            title: 'Error editando el post',
            description: r.error.message,
            code: 500,
          },
        });
      },
    });
  }

  managePostImage() {
    if (!this.post) {
      this.router.navigate(['/error'], {
        queryParams: {
          title: 'Error editando la imagen del post',
          description:
            'No se ha podido editar la imagen del post porque no se ha encontrado el identificador del post.',
          code: 500,
        },
      });
      return;
    }
    if (this.hasToDeleteImage) {
      this.postService.deletePostImage(this.post.identifier).subscribe({
        next: (response: string) => {
          if (this.post) {
            this.post.hasImage = false; // Update the post object to reflect the image deletion
            this.selectedImageURL = null; // Clear the selected image URL if it exists
            window.location.href = '/post/' + this.post.identifier;
          }
        },
        error: (r) => {
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error eliminando la imagen del post',
              description: r.error.message,
              code: 500,
            },
          });
        },
      });
    } else {
      this.setPostImage();
    }
  }

  setPostImage() {
    if (!this.post) {
      this.router.navigate(['/error'], {
        queryParams: {
          title: 'Error editando la imagen del post',
          description:
            'No se ha podido editar la imagen del post porque no se ha encontrado el identificador del post.',
          code: 500,
        },
      });
      return;
    }
    if (this.selectedImageURL) {
      const input = document.querySelector(
        'input[type="file"]'
      ) as HTMLInputElement;
      if (input.files && input.files.length > 0) {
        const file = input.files[0];
        this.postService.updatePostImage(this.post.identifier, file).subscribe({
          next: () => {
            if (!this.post) {
              return;
            }
            this.selectedImageURL = null; // Clear the selected image URL
            window.location.href = '/post/' + this.post.identifier;
          },
          error: (r) => {
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Error editando la imagen del post',
                description: r.error.message,
                code: 500,
              },
            });
          },
        });
      }
    } else {
      window.location.href = '/post/' + this.post.identifier;
    }
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
      this.router.navigate(['/error'], {
        queryParams: {
          title: 'Error eliminando la imagen del post',
          description:
            'No se ha podido eliminar la imagen del post porque no se ha encontrado el identificador del post.',
          code: 500,
        },
      });
      return;
    }
    if (!this.post) {
      this.router.navigate(['/error'], {
        queryParams: {
          title: 'Error eliminando la imagen del post',
          description:
            'No se ha podido eliminar la imagen del post porque no se ha encontrado el post.',
          code: 500,
        },
      });
      return;
    }
    if (this.post.hasImage) {
      this.hasToDeleteImage = true;
      this.selectedImageURL = null;
    } else if (this.selectedImageURL) {
      this.selectedImageURL = null;
    }
  }

  uploadPostImage(postID: number | undefined, file: File): void {
    if (!postID) {
      this.router.navigate(['/error'], {
        queryParams: {
          title: 'Error subiendo la imagen del post',
          description:
            'No se ha podido subir la imagen del post porque no se ha encontrado el identificador del post.',
          code: 500,
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
            title: 'Error subiendo la imagen del post',
            description: r.error.message,
            code: 500,
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
        this.hasToDeleteImage = false;
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
    // Save the current selection
    const selection = window.getSelection();
    if (selection && selection.rangeCount > 0) {
      this.savedSelection = selection.getRangeAt(0);
    } else {
      // If no text is selected, create a new selection at the cursor position
      const postContentElement = document.getElementById('postContent');
      if (postContentElement) {
        const range = document.createRange();
        range.selectNodeContents(postContentElement);
        range.collapse(false); // collapse to end
        this.savedSelection = range;
      }
    }

    this.showHyperlinkModal = true;
    setTimeout(() => {
      const modalElement = document.getElementById('hyperlinkModal');
      if (modalElement) {
        modalElement.classList.add('show');
      }
    }, 0);
  }

  closeHyperlinkModal(): void {
    const modalElement = document.getElementById('hyperlinkModal');
    if (modalElement) {
      modalElement.classList.remove('show');
      setTimeout(() => {
        this.showHyperlinkModal = false;
        this.hyperlinkURL = '';
        this.savedSelection = null;
      }, 300); // Match the duration of the CSS transition
    }
  }

  insertHyperlink(): void {
    if (!this.hyperlinkURL || !this.savedSelection) {
      return;
    }

    // Ensure the URL has a protocol
    let finalUrl = this.hyperlinkURL;
    if (!this.hyperlinkURL.startsWith('http://') && !this.hyperlinkURL.startsWith('https://')) {
      finalUrl = 'https://' + this.hyperlinkURL;
    }

    // Restore the selection
    const selection = window.getSelection();
    if (selection) {
      selection.removeAllRanges();
      selection.addRange(this.savedSelection);
    }

    // Create and insert the link
    const range = this.savedSelection;
    const selectedText = range.toString() || finalUrl;
    const anchor = document.createElement('a');
    anchor.href = finalUrl;
    anchor.target = '_blank'; // Open in new tab
    anchor.rel = 'noopener noreferrer'; // Security best practice
    anchor.textContent = selectedText;
    range.deleteContents();
    range.insertNode(anchor);

    // Refocus the post content
    this.refocusPostContent();

    this.closeHyperlinkModal();
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
