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
import { Ban } from '../../models/ban.model';
import { Router } from '@angular/router';

Chart.register(...registerables);

@Component({
  selector: 'app-modify-user',
  templateUrl: './modifyUser.component.html',
  styleUrls: ['./modifyUser.component.css', '../../../animations.css'],
  providers: [DatePipe],
})
export class ModifyUserComponent implements OnInit {
  showModal: boolean = false;

  showAdvancedMenu: boolean = false;

  profileUser: User | undefined;

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

  confirmationModalText: string = '';

  newAlias: string = '';
  newDescription: string = '';

  showEmailField: boolean = false;
  showPasswordFields: boolean = false;

  currentEmail: string = '';
  newEmail: string = '';
  newPassword: string = '';
  newPasswordConfirm: string = '';

  selectedImageURL: string | ArrayBuffer | null = '';

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
    this.checkIfLoggedIn();
  }

  // User profile data (doesn't have to be the logged user)
  loadProfile() {
    let username = this.route.snapshot.paramMap.get('username');
    // if the logged user is not the same as the profile user or the logged user is not an admin, redirect to error page
    if (this.loggedUsername != username && !this.isAdmin) {
      this.router.navigate(['/error'], {
        queryParams: {
          title: 'Error de autenticación',
          description: 'No tienes permisos para acceder a esta página.',
          code: 403,
        },
      });
    }
    if (username) {
      this.profileService.getUser(username).subscribe({
        next: (user) => {
          this.profileUser = user;
          this.currentEmail = user.email;
        },
        error: (r) => {
          // if error is 404, redirect to error page with a custom message
          if (r.status == 404) {
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Usuario no encontrado',
                description: 'El usuario que buscas no existe.',
                code: '404',
              },
            });
          } else {
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Error cargando perfil',
                description: r.error.message,
                code: r.status,
              },
            });
          }
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
                  title: 'Error cargando usuario',
                  description: r.error.message,
                  code: r.status,
                },
              });
            },
          });
        } else {
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error de autenticación',
              description: 'Debes iniciar sesión para acceder a esta página.',
              code: 401,
            },
          });
        }
      },
      error: (r) => {
        this.router.navigate(['/error'], {
          queryParams: {
            title: 'Error cargando usuario',
            description: r.error.message,
            code: r.status,
          },
        });
      },
    });
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
    this.alertModalText = '';
    this.showEmailField = false;
    this.showPasswordFields = false;
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

  showChangeEmailModal() {
    this.showEmailField = true;
    this.openAlertModal(
      '',
      () => {
        if (!this.profileUser) {
          return;
        }
        if (this.newEmail.length == 0) {
          this.alertModalText = 'El campo de email no puede estar vacío.';
          return;
        }
        this.profileService
          .editUser(this.profileUser.username, {
            email: this.newEmail,
          })
          .subscribe({
            next: () => {
              this.closeAlertModal();
              this.showEmailField = false;
              this.newEmail = '';
              this.openConfirmationModal('E-mail cambiado con éxito.');
            },
            error: (r) => {
              this.router.navigate(['/error'], {
                queryParams: {
                  title: 'Error cambiando e-mail',
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

  checkPasswordRequirements(password: string): boolean {
    // Password requirements
    // At least 8 characters
    // At least 1 digit
    // At least 1 lowercase letter
    // At least 1 uppercase letter
    // At least 1 special character
    return /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$/.test(
      password
    );
  }

  showChangePasswordModal() {
    this.showPasswordFields = true;
    this.openAlertModal(
      '',
      () => {
        if (!this.profileUser) {
          return;
        }
        if (this.newPassword.length == 0) {
          this.alertModalText = 'La contraseña no puede estar vacía.';
          return;
        }
        if (this.newPassword != this.newPasswordConfirm) {
          this.alertModalText = 'Las contraseñas no coinciden.';
          return;
        }
        if (!this.checkPasswordRequirements(this.newPassword)) {
          this.alertModalText =
            'La contraseña debe tener al menos 8 caracteres, una letra mayúscula, una letra minúscula, un número y un carácter especial.';
          return;
        }
        this.profileService
          .editUser(this.profileUser.username, {
            password: this.newPassword,
          })
          .subscribe({
            next: () => {
              this.closeAlertModal();
              this.showPasswordFields = false;
              this.newPassword = '';
              this.newPasswordConfirm = '';
              this.openConfirmationModal('Contraseña cambiada con éxito.');
            },
            error: (r) => {
              this.router.navigate(['/error'], {
                queryParams: {
                  title: 'Error cambiando contraseña',
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

  openConfirmationModal(text: string) {
    this.confirmationModalText = text;
    this.openAlertModal(
      '',
      () => {
        this.closeAlertModal();
      },
      false
    );
  }

  discardSelectedImage(): void {
    this.selectedImageURL = null;
  }

  manageProfilePicture() {
    if (!this.profileUser) {
      return;
    }
    if (this.selectedImageURL) {
      const input = document.querySelector(
        'input[type="file"]'
      ) as HTMLInputElement;
      if (input.files && input.files.length > 0) {
        const file = input.files[0];
        this.profileService
          .uploadProfilePicture(this.profileUser.username, file)
          .subscribe({
            next: () => {
              window.location.href = '/profile/' + this.profileUser?.username;
            },
            error: (r) => {
              this.router.navigate(['/error'], {
                queryParams: {
                  title: 'Error subiendo imagen',
                  description: r.error.message,
                  code: r.status,
                },
              });
            },
          });
      }
    } else {
      window.location.href = '/profile/' + this.profileUser?.username;
    }
  }

  saveChanges() {
    if (!this.profileUser) {
      return;
    }
    const userInfo: any = {};
    if (this.newAlias.length > 0) {
      userInfo.alias = this.newAlias;
    }
    if (this.newDescription.length > 0) {
      userInfo.description = this.newDescription;
    }
    this.profileService
      .editUser(this.profileUser.username, userInfo)
      .subscribe({
        next: () => {
          this.manageProfilePicture();
        },
        error: (r) => {
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error editando usuario',
              description: r.error.message,
              code: r.status,
            },
          });
        },
      });
  }
}
