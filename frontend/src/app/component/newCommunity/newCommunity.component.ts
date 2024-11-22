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
  selector: 'app-new-community',
  templateUrl: './newCommunity.component.html',
  styleUrls: ['./newCommunity.component.css', '../../../animations.css'],
  providers: [DatePipe],
})
export class NewCommunityComponent implements OnInit {
  showModal: boolean = false;

  admin: User | undefined;

  community: Community | undefined;

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

  showDescription: boolean = false;

  // COMMUNITY EDITOR
  communityName: string = '';
  communityDescription: string = '';
  communityNameMaxLength = 50;
  communityDescriptionMaxLength = 500;
  wantsToDeleteBanner: boolean = false;

  selectedImageURL: string | ArrayBuffer | null = null;

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
  }

  loadUserData(user: User) {
    this.user = user;
    this.loggedUsername = user.username;
    this.isAdmin = user.roles.includes('ADMIN');
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
              description: 'Debes iniciar sesión para crear una comunidad.',
              code: 401,
            },
          });
        }
      },
      error: (r) => {
        // if error is 401, user is not logged in, do not print error
        if (r.status != 401) {
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error al comprobar la sesión',
              description: r.error.message,
              code: r.status,
            },
          });
        }
      },
    });
  }

  communityBannerURL(communityID: number | undefined): string {
    if (this.selectedImageURL) {
      return this.selectedImageURL as string;
    }
    if (!communityID) {
      return '';
    }
    if (this.wantsToDeleteBanner) {
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

  toggleCommunityDescription() {
    this.showDescription = !this.showDescription;
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      const reader = new FileReader();
      reader.onload = () => {
        this.selectedImageURL = reader.result; // Set the selected image URL
        this.wantsToDeleteBanner = false;
      };
      reader.readAsDataURL(file);
    }
  }

  // COMMUNITY EDITOR

  preventExceedingContent(event: KeyboardEvent) {
    const communityName = document.getElementById(
      'communityName'
    ) as HTMLInputElement;

    const isTyping =
      !event.ctrlKey &&
      !event.metaKey &&
      !event.altKey &&
      event.key.length === 1;

    if (isTyping) {
      if (
        communityName.value.length >= this.communityNameMaxLength &&
        event.target === communityName
      ) {
        event.preventDefault();
        console.log('Prevented exceeding content');
      }
    }
  }

  checkContentLength() {
    const communityName = document.getElementById(
      'communityName'
    ) as HTMLInputElement;

    // Check and trim the length of the community name
    if (
      communityName &&
      communityName.value.length > this.communityNameMaxLength
    ) {
      communityName.value = communityName.value.slice(
        0,
        this.communityNameMaxLength
      );
    }

    // Check and trim the length of the community description
    const communityDescription = document.getElementById(
      'communityDescription'
    ) as HTMLTextAreaElement;
    if (
      communityDescription &&
      communityDescription.value.length > this.communityDescriptionMaxLength
    ) {
      communityDescription.value = communityDescription.value.slice(
        0,
        this.communityDescriptionMaxLength
      );
    }
  }

  adjustTextareaHeight(event: Event): void {
    const textarea = event.target as HTMLTextAreaElement;
    const scrollTop = window.scrollY || document.documentElement.scrollTop;
    textarea.style.height = 'auto';
    textarea.style.height = `${textarea.scrollHeight}px`;
    window.scrollTo(0, scrollTop);
  }

  deleteBanner() {
    if (this.selectedImageURL) {
      this.selectedImageURL = null;
    } else {
      this.wantsToDeleteBanner = !this.wantsToDeleteBanner;
    }
  }

  setBanner() {
    if (!this.community) {
      return;
    }
    // Upload the image if selected
    if (this.selectedImageURL) {
      const input = document.querySelector(
        'input[type="file"]'
      ) as HTMLInputElement;
      if (input.files && input.files.length > 0) {
        const file = input.files[0];
        this.communityService
          .updateCommunityBanner(this.community.identifier, file, undefined)
          .subscribe({
            next: () => {
              this.showDoneModal();
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
    } else {
      this.showDoneModal();
    }
  }

  showDoneModal() {
    this.openAlertModal(
      '¡Los cambios han sido guardados!',
      () => {
        window.location.href = '/community/' + this.community?.identifier;
      },
      false
    );
  }

  manageBanner() {
    if (this.wantsToDeleteBanner) {
      if (!this.community) {
        return;
      }
      this.communityService
        .updateCommunityBanner(this.community.identifier, undefined, 'delete')
        .subscribe({
          next: () => {
            this.showDoneModal();
          },
          error: (r) => {
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Error al borrar la imagen',
                description: r.error.message,
                code: r.status,
              },
            });
          },
        });
    } else {
      this.setBanner();
    }
  }

  confirmChanges() {
    // Check if the community name is empty
    if (this.communityName.trim() === '') {
      this.openAlertModal(
        'El nombre de la comunidad no puede estar vacío.',
        () => {},
        false
      );
      return;
    }

    const communityData = {
      name: this.communityName,
      description: this.communityDescription,
    };

    this.communityService.createCommunity(communityData).subscribe({
      next: (community) => {
        this.community = community;
        this.manageBanner();
      },
      error: (r) => {
        // if error code is 409, the community name is already taken
        if (r.status === 409) {
          this.openAlertModal(
            'El nombre de la comunidad ya está en uso.',
            () => {},
            false
          );
        } else {
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error al crear la comunidad',
              description: r.error.message,
              code: r.status,
            },
          });
        }
      },
    });
  }
}
