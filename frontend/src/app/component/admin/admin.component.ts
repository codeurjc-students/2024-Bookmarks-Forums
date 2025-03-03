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
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css', '../../../animations.css'],
  providers: [DatePipe],
})
export class AdminComponent implements OnInit {
  showModal: boolean = false;
  showUsersModal: boolean = false;
  usersModalMode: number = 0; // 1 -> followers, 2 -> following
  usersModalTitle: string = 'Siguiendo'; // Default title

  showCommunitiesModal: boolean = false;
  communitiesModalMode: boolean = false; // true -> administrated, false -> joined
  communitiesModalTitle: string = 'Comunidades'; // Default title

  usersModalList: User[] = [];
  usersModalListPage: number = 0;
  usersModalListSize: number = 10;
  usersModalListNoMore: boolean = false;
  usersModalListLoading: boolean = false;

  showAdvancedMenu: string | null = null;

  searchTerm: string = '';

  title = 'Bookmarks';
  userLoaded = false;

  user: User | undefined;
  loggedUsername: string = '';
  loggedIn: boolean = false;
  isAdmin: boolean = false;

  public chart: any;

  // users pagination
  size = 10;
  page = 0;

  // Users list
  users: User[] = [];
  loadingMoreUsers = false;
  noMoreUsers = false;

  // Disable user options
  disableDurations = [
    { value: 'day', label: '1 día' },
    { value: 'week', label: '1 semana' },
    { value: '2weeks', label: '2 semanas' },
    { value: 'month', label: '1 mes' },
    { value: '6months', label: '6 meses' },
    { value: 'year', label: '1 año' },
    { value: 'forever', label: 'Permanente' }
  ];
  selectedDuration: string = '86400';

  showAlertModal: boolean = false;
  alertModalText: string = '';
  confirmAction: () => void = () => {};
  showCancelButton: boolean = true;

  // Alert modal properties
  alertModalTitle: string = '';
  alertModalMessage: string = '';
  alertModalConfirmText: string = '';
  alertModalCancelText: string = '';
  alertModalType: string = '';
  alertModalVisible: boolean = false;
  alertModalConfirmAction: () => void = () => {};
  isDisablingUser: boolean = false; // Variable para controlar si estamos deshabilitando o habilitando

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
    this.loadBansChart();
    this.loadDislikesChart();
  }

  // Logged user data
  loadUserData(user: User) {
    this.user = user;
    this.loggedUsername = user.username;
    this.isAdmin = user.roles.includes('ADMIN');
    this.loadUsers();
  }

  loadBansChart() {
    let usernames: string[] = [];
    let bansCount: number[] = [];

    this.profileService.getMostBannedUsers(10).subscribe({
      next: (users) => {
        users.forEach((user) => {
          usernames.push(user[0]);
          bansCount.push(user[1]);
        });

        // Create the chart
        new Chart('mostBannedUsersChart', {
          type: 'bar',
          data: {
            labels: usernames,
            datasets: [
              {
                label: 'Número de baneos',
                data: bansCount,
                backgroundColor: '#F8DDA4',
                borderWidth: 1,
              },
            ],
          },
          options: {
            scales: {
              y: {
                beginAtZero: true,
                ticks: {
                  callback: function (value, index, values) {
                    return Math.round(value as number);
                  },
                  color: '#91818A',
                },
                grid: {
                  display: false,
                },
              },
              x: {
                ticks: {
                  color: '#efefef',
                },
                grid: {
                  display: false,
                },
              },
            },
            plugins: {
              legend: {
                labels: {
                  color: '#efefef',
                },
              },
            },
          },
        });
      },
      error: (r) => {
        this.router.navigate(['/error'], {
          queryParams: {
            title: 'Error obteniendo usuarios con más baneos',
            description: r.error.message,
            code: r.status,
          },
        });
      },
    });
  }

  loadDislikesChart() {
    let usernames: string[] = [];
    let dislikesCount: number[] = [];

    this.profileService.getMostDislikedUsers(10).subscribe({
      next: (users) => {
        users.forEach((user) => {
          usernames.push(user[0]);
          dislikesCount.push(user[1]);
        });

        // Create the chart
        new Chart('mostDislikedUsersChart', {
          type: 'bar',
          data: {
            labels: usernames,
            datasets: [
              {
                label: 'Número de dislikes',
                data: dislikesCount,
                backgroundColor: '#F8DDA4',
                borderWidth: 1,
              },
            ],
          },
          options: {
            scales: {
              y: {
                beginAtZero: true,
                ticks: {
                  callback: function (value, index, values) {
                    return Math.round(value as number);
                  },
                  color: '#91818A',
                },
                grid: {
                  display: false,
                },
              },
              x: {
                ticks: {
                  color: '#efefef',
                },
                grid: {
                  display: false,
                },
              },
            },
            plugins: {
              legend: {
                labels: {
                  color: '#efefef',
                },
              },
            },
          },
        });
      },
      error: (r) => {
        this.router.navigate(['/error'], {
          queryParams: {
            title: 'Error obteniendo usuarios con más dislikes',
            description: r.error.message,
            code: r.status,
          },
        });
      },
    });
  }

  loadUsers() {
    if (!this.isAdmin) {
      return;
    }
    
    this.profileService.getUsersByBanCount(this.searchTerm, this.page, this.size).subscribe({
      next: (users) => {
        if (!users || users.length === 0) {
          this.noMoreUsers = true;
          this.loadingMoreUsers = false;
          return;
        }
        this.users = this.users.concat(users);
        this.page++;
        this.noMoreUsers = users.length < this.size;
        this.loadingMoreUsers = false;
      },
      error: (r) => {
        this.router.navigate(['/error'], {
          queryParams: {
            title: 'Error al obtener los usuarios',
            description: r.error.message,
            code: r.status,
          },
        });
      },
    });
  }

  loadMoreUsers() {
    this.loadingMoreUsers = true;
    this.loadUsers();
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
            title: 'Error al obtener el post',
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
              title: 'Error al obtener la imagen del post',
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
                  title: 'Error al obtener el usuario logueado',
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
          this.router.navigate(['/login']);
        }
      },
      error: (r) => {
        // if error is 401, user is not logged in, do not print error
        if (r.status != 401) {
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error al comprobar si estás logueado',
              description: r.error.message,
              code: r.status,
            },
          });
        }
        this.router.navigate(['/login']);
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

  searchUsers() {
    this.users = [];
    this.page = 0;
    this.noMoreUsers = false;
    this.loadingMoreUsers = true;
    this.loadUsers();
    this.loadingMoreUsers = false;
  }

  clearSearch() {
    this.searchTerm = '';
    this.users = [];
    this.page = 0;
    this.noMoreUsers = false;
    this.loadingMoreUsers = true;
    this.loadUsers();
    this.loadingMoreUsers = false;
  }

  toggleAdvancedMenu(username: string) {
    this.showAdvancedMenu = this.showAdvancedMenu === username ? null : username;
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

  disableUser(username: string) {
    this.alertModalTitle = '¿Estás seguro?';
    this.alertModalMessage = `¿Estás seguro de que quieres deshabilitar la cuenta de ${username}?`;
    this.alertModalConfirmText = 'Deshabilitar';
    this.alertModalCancelText = 'Cancelar';
    this.alertModalType = 'warning';
    this.alertModalVisible = true;
    this.isDisablingUser = true; // Estamos deshabilitando
    this.alertModalConfirmAction = () => {
      this.profileService.disableUser(username, this.selectedDuration).subscribe({
        next: () => {
          const userIndex = this.users.findIndex(user => user.username === username);
          if (userIndex !== -1) {
            this.users[userIndex].isDisabled = true;
            this.users[userIndex].disabledUntil = this.selectedDuration === 'forever' ? '' : new Date(Date.now() + parseInt(this.selectedDuration) * 1000).toISOString();
          }
          this.alertModalVisible = false;
        },
        error: (error) => {
          console.error('Error disabling user:', error);
          this.router.navigate(['/error']);
        }
      });
    };
  }

  enableUser(username: string) {
    this.alertModalTitle = '¿Estás seguro?';
    this.alertModalMessage = `¿Estás seguro de que quieres habilitar la cuenta de ${username}?`;
    this.alertModalConfirmText = 'Habilitar';
    this.alertModalCancelText = 'Cancelar';
    this.alertModalType = 'warning';
    this.alertModalVisible = true;
    this.isDisablingUser = false; // No estamos deshabilitando
    this.alertModalConfirmAction = () => {
      this.profileService.enableUser(username).subscribe({
        next: () => {
          const userIndex = this.users.findIndex(user => user.username === username);
          if (userIndex !== -1) {
            this.users[userIndex].isDisabled = false;
            this.users[userIndex].disabledUntil = '';
          }
          this.alertModalVisible = false;
        },
        error: (error) => {
          console.error('Error enabling user:', error);
          this.router.navigate(['/error']);
        }
      });
    };
  }

  setDisableDuration(duration: string) {
    this.selectedDuration = duration;
  }

  formatDisabledUntil(date: string | undefined): string {
    if (!date) {
      return 'Permanente';
    }
    return new Date(date).toLocaleDateString('es-ES', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getSelectedDurationLabel(): string {
    const duration = this.disableDurations.find(d => d.value === this.selectedDuration);
    return duration ? duration.label : 'Seleccionar duración';
  }
}
