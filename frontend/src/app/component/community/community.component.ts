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
import { Ban } from '../../models/ban.model';
import { Router } from '@angular/router';
import { TitleService } from '../../services/title.service';
Chart.register(...registerables);

@Component({
  selector: 'app-community',
  templateUrl: './community.component.html',
  styleUrls: ['./community.component.css', '../../../animations.css'],
  providers: [DatePipe],
})
export class CommunityComponent implements OnInit, OnDestroy {
  showModal: boolean = false;

  selectedOrderText: string = 'Fecha de creación'; // Default text

  showAdvancedMenu: boolean = false;

  showBanModal: boolean = false;
  banReasonValue: string = '';
  banDurationText: string = 'day';
  banDurationTextTranslated: string = '1 día';
  usernameToBan: string = '';

  searchTerm: string = '';
  sortCriteria: string = 'default'; // Default search criteria
  sortCriteriaText: string = 'Más antiguos'; // Default search criteria text

  communityPostsCount: number = 0;
  posts: Post[] = [];

  admin: User | undefined;

  hasModerators: boolean = false;
  moderators: User[] = [];
  moderatorsPage = 0;
  moderatorsSize = 5;
  loadingMoreModerators = false;
  noMoreModerators = false;

  community: Community | undefined;
  communityMembersCount: number = 0;
  communityMembers: User[] = [];

  title = 'Bookmarks';
  userLoaded = false;

  user: User | undefined;
  loggedUsername: string = '';
  loggedIn: boolean = false;
  isAdmin: boolean = false;
  isMember: boolean = false;
  isCommunityAdmin: boolean = false;
  isModerator: boolean = false;

  public chart: any;

  // posts pagination
  size = 10;
  page = 0;

  membersSearchTerm: string = '';

  // members pagination
  membersSize = 10;
  membersPage = 0;

  loadingMoreMembers = false;
  noMoreMembers = false;

  loadingMorePosts = false;
  noMorePosts = false;

  showAlertModal: boolean = false;
  alertModalText: string = '';
  confirmAction: () => void = () => {};
  showCancelButton: boolean = true;

  showDescription: boolean = false;

  showBanManagerModal: boolean = false;
  bannedUsers: Ban[] = [];
  bannedUsersPage: number = 0;
  bannedUsersSize: number = 1;
  loadingMoreBannedUsers: boolean = false;
  noMoreBannedUsers: boolean = false;
  banToUnban: Ban | undefined = undefined;

  isUserBanned: boolean = false;
  userBan: Ban | undefined;
  showUserBanInfo: boolean = false;

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
    this.titleService.setTitle('Comunidad');
    this.checkIfLoggedIn();
  }

  loadUserData(user: User) {
    this.user = user;
    this.loggedUsername = user.username;
    this.isAdmin = user.roles.includes('ADMIN');
    this.loadCommunity();
  }

  getAdmin() {
    if (this.community) {
      this.communityService.getAdmin(this.community.identifier).subscribe({
        next: (admin) => {
          this.admin = admin;
        },
        error: (r) => {
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error obteniendo administrador de la comunidad',
              description: r.error.message,
              code: 500,
            },
          });
        },
      });
    }
  }

  loadModerators() {
    if (this.community) {
      this.communityService
        .getModerators(
          this.community.identifier,
          this.moderatorsPage,
          this.moderatorsSize
        )
        .subscribe({
          next: (moderators) => {
            if (!moderators || moderators.length == 0) {
              this.noMoreModerators = true;
              this.loadingMoreModerators = false;
              return;
            }
            this.moderators = this.moderators.concat(moderators);
            this.hasModerators = true;
            this.loadingMoreModerators = false;
            this.moderatorsPage += 1;
            this.noMoreModerators = moderators.length < this.moderatorsSize;
          },
          error: (r) => {
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Error obteniendo moderadores de la comunidad',
                description: r.error.message,
                code: 500,
              },
            });
          },
        });
    } else {
      this.router.navigate(['/error'], {
        queryParams: {
          title: 'Error obteniendo moderadores de la comunidad',
          description: 'No se ha podido obtener la comunidad',
          code: 500,
        },
      });
    }
  }

  loadMoreModerators() {
    this.loadingMoreModerators = true;
    this.loadModerators();
    this.loadingMoreModerators = false;
  }

  loadBan(banID: number) {
    if (this.community) {
      this.communityService.getBan(banID).subscribe({
        next: (ban) => {
          this.userBan = ban;
        },
        error: (r) => {
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error obteniendo información del ban',
              description: r.error.message,
              code: 500,
            },
          });
        },
      });
    }
  }

  loadUserBan() {
    if (this.community && this.loggedUsername) {
      this.communityService
        .isUserBanned(this.community.identifier, this.loggedUsername)
        .subscribe({
          next: (banID) => {
            if (banID !== -1) {
              this.isUserBanned = true;
              this.loadBan(banID);
            } else {
              this.isUserBanned = false;
            }
          },
          error: (r) => {
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Error obteniendo información del ban',
                description: r.error.message,
                code: 500,
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
        this.titleService.setTitle(`${community.name}`);
        this.isUserMember();
        this.getMembers();
        this.getMembersCount();
        this.getAdmin();
        this.loadModerators();
        this.getPostsCount();
        this.loadPosts();
      },
      error: (r) => {
        // if error is 404, community does not exist
        if (r.status == 404) {
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Comunidad no encontrada',
              description: 'La comunidad que buscas no existe',
              code: 404,
            },
          });
        } else {
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error obteniendo comunidad',
              description: r.error.message,
              code: 500,
            },
          });
        }
      },
    });
  }

  loadPosts(searchTerm?: string) {
    if (this.community) {
      this.communityService
        .getPosts(
          this.community.identifier,
          this.page,
          this.size,
          this.sortCriteria,
          searchTerm
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
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Error obteniendo posts de la comunidad',
                description: r.error.message,
                code: 500,
              },
            });
          },
        });
    } else {
      this.router.navigate(['/error'], {
        queryParams: {
          title: 'Error obteniendo posts de la comunidad',
          description: 'No se ha podido obtener la comunidad',
          code: 500,
        },
      });
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
        this.router.navigate(['/error'], {
          queryParams: {
            title: 'Error obteniendo post',
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
          return false;
        },
      });
    }
  }

  checkModerator() {
    if (this.community) {
      this.communityService
        .isModerator(this.community.identifier, this.loggedUsername)
        .subscribe({
          next: (isModerator) => {
            this.isModerator = isModerator;
          },
          error: (r) => {
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Error comprobando si el usuario es moderador',
                description: r.error.message,
                code: 500,
              },
            });
          },
        });
    }
  }

  isUserMember() {
    if (this.community && this.loggedUsername) {
      this.communityService
        .isUserMember(this.community.identifier, this.loggedUsername)
        .subscribe({
          next: (isMember) => {
            this.isMember = isMember;
            this.isCommunityAdmin =
              this.community?.admin.username === this.loggedUsername;
            this.loadUserBan();
            this.checkModerator();
          },
          error: (r) => {
            // if the error is unauthorized, the user is not a member
            if (r.status == 401) {
              this.isMember = false;
            } else {
              this.router.navigate(['/error'], {
                queryParams: {
                  title: 'Error comprobando si el usuario es miembro',
                  description: r.error.message,
                  code: 500,
                },
              });
            }
            this.loadUserBan();
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
              this.loadCommunity();
            },
          });
        } else {
          // if user is not logged in
          this.loggedUsername = ''; // set the logged username to empty
          this.user = undefined;
          this.isAdmin = false;
          this.isMember = false;
          this.isCommunityAdmin = false;
          this.isModerator = false;
          this.loadCommunity();
        }
      },
      error: (r) => {
        // if error is 401, user is not logged in, do not print error
        if (r.status != 401) {
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error comprobando si el usuario está logueado',
              description: r.error.message,
              code: 500,
            },
          });
        }
        this.loadCommunity();
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

  searchPosts() {
    if (!this.community) {
      return;
    }
    this.posts = [];
    this.page = 0;
    this.noMorePosts = false;
    this.loadingMorePosts = true;
    this.communityService
      .getPosts(
        this.community.identifier,
        this.page,
        this.size,
        this.sortCriteria,
        this.searchTerm
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
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error buscando posts',
              description: r.error.message,
              code: 500,
            },
          });
        },
      });
  }

  clearSearch() {
    this.searchTerm = '';
    this.posts = [];
    this.page = 0;
    this.noMorePosts = false;
    this.loadingMorePosts = true;
    this.loadPosts();
    this.loadingMorePosts = false;
  }

  setSortCriteria(criteria: string) {
    this.sortCriteria = criteria;
    if (criteria === 'lastModifiedDate') {
      this.sortCriteriaText = 'Última modificación';
    } else if (criteria === 'creationDate') {
      this.sortCriteriaText = 'Más recientes';
    } else if (criteria === 'likes') {
      this.sortCriteriaText = 'Mejor votados';
    } else if (criteria === 'replies') {
      this.sortCriteriaText = 'Más comentados';
    } else {
      this.sortCriteriaText = 'Más antiguos';
    }
    this.posts = [];
    this.page = 0;
    this.noMorePosts = false;
    this.loadingMorePosts = true;
    this.loadPosts(this.searchTerm);
    this.loadingMorePosts = false;
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
      this.communityService
        .getMembers(
          this.community.identifier,
          this.membersPage,
          this.membersSize
        )
        .subscribe({
          next: (members) => {
            if (!members || members.length == 0) {
              this.noMoreMembers = true;
              this.loadingMoreMembers = false;
              return;
            }
            this.communityMembers = members;
            this.loadingMoreMembers = false;
            this.membersPage += 1;
            this.noMoreMembers = members.length < this.size;
          },
          error: (r) => {
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Error obteniendo miembros de la comunidad',
                description: r.error.message,
                code: 500,
              },
            });
          },
        });
    } else {
      this.router.navigate(['/error'], {
        queryParams: {
          title: 'Error obteniendo miembros de la comunidad',
          description: 'No se ha podido obtener la comunidad',
          code: 500,
        },
      });
    }
  }

  loadMoreMembers() {
    this.loadingMoreMembers = true;
    this.getMembers();
    this.loadingMoreMembers = false;
  }

  getMembersCount() {
    if (this.community) {
      this.communityService
        .getMembersCount(this.community.identifier)
        .subscribe({
          next: (count) => {
            this.communityMembersCount = count;
          },
          error: (r) => {
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Error obteniendo miembros de la comunidad',
                description: r.error.message,
                code: 500,
              },
            });
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
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error obteniendo posts de la comunidad',
              description: r.error.message,
              code: 500,
            },
          });
        },
      });
    }
  }

  joinOrLeaveCommunity() {
    if (this.community) {
      if (this.isMember) {
        this.communityService
          .leaveCommunity(this.community.identifier, this.loggedUsername)
          .subscribe({
            next: () => {
              this.isMember = false;
              this.communityMembers = this.communityMembers.filter(
                (m) => m.username !== this.loggedUsername
              );
              this.moderators = this.moderators.filter(
                (m) => m.username !== this.loggedUsername
              );
              this.getMembersCount();
            },
            error: (r) => {
              if (r.status == 401) {
                this.openAlertModal(
                  'Debes traspasar tus poderes de administrador a otro usuario para poder abandonar esta comunidad',
                  () => {},
                  false
                );
              } else {
                this.router.navigate(['/error'], {
                  queryParams: {
                    title: 'Error abandonando comunidad',
                    description: r.error.message,
                    code: 500,
                  },
                });
              }
            },
          });
      } else {
        this.communityService
          .joinCommunity(this.community.identifier, this.loggedUsername)
          .subscribe({
            next: () => {
              this.isMember = true;
              this.reloadAllMembersList();
            },
            error: (r) => {
              this.router.navigate(['/error'], {
                queryParams: {
                  title: 'Error uniéndose a la comunidad',
                  description: r.error.message,
                  code: 500,
                },
              });
            },
          });
      }
    }
  }

  removeMember(username: string) {
    // admin can't remove themselves
    if (username === this.community?.admin.username) {
      if (this.loggedUsername === this.community?.admin.username) {
        this.openAlertModal(
          'Debes traspasar tus poderes de administrador a otro usuario para poder abandonar esta comunidad',
          () => {},
          false
        );
        return;
      }
      if (this.isAdmin) {
        this.openAlertModal(
          'Para expulsar a este usuario, conviértete en Administrador de la comunidad.',
          () => {},
          false
        );
      } else {
        this.openAlertModal(
          '¿Qué sería de un reino sin su rey? No puedes expulsar al administrador de la comunidad.',
          () => {},
          false
        );
      }
      return;
    }
    if (this.community) {
      this.communityService
        .leaveCommunity(this.community.identifier, username)
        .subscribe({
          next: () => {
            this.communityMembers = this.communityMembers.filter(
              (m) => m.username !== username
            );
            this.communityMembersCount -= 1;

            this.moderators = this.moderators.filter(
              (m) => m.username !== username
            );
          },
          error: (r) => {
            this.openAlertModal(
              'Error al expulsar al usuario: no puedes expulsar a otros moderadores.',
              () => {},
              false
            );
          },
        });
    }
  }

  closeBanModal() {
    this.showBanModal = false;
  }

  confirmBanModal() {
    if (!this.community) {
      return;
    }

    this.communityService
      .banUser(
        this.community.identifier,
        this.usernameToBan,
        this.banReasonValue,
        this.banDurationText
      )
      .subscribe({
        next: () => {
          this.showBanModal = false;
          this.openAlertModal(
            '¡' + this.usernameToBan + ' ha sido expulsado de la comunidad!',
            () => {},
            false
          );
          this.reloadAllMembersList();
        },
        error: (r) => {
          // if error code == forbidden, user is trying to ban site admin
          if (r.status == 403) {
            this.openAlertModal(
              'No puedes banear a un administrador del sitio.',
              () => {},
              false
            );
            return;
          }
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error baneando usuario',
              description: r.error.message,
              code: 500,
            },
          });
        },
      });
  }

  banMember(username: string) {
    // admin can't ban themselves
    if (username === this.community?.admin.username) {
      if (this.loggedUsername === this.community?.admin.username) {
        this.openAlertModal(
          'No tiene sentido lo que estás haciendo...',
          () => {},
          false
        );
        return;
      }
      if (this.isAdmin) {
        this.openAlertModal(
          'Para banear a este usuario, conviértete en Administrador de la comunidad',
          () => {},
          false
        );
      } else {
        this.openAlertModal(
          '¿Qué sería de un reino sin su rey? No puedes expulsar al administrador de la comunidad',
          () => {},
          false
        );
      }
      return;
    }
    this.usernameToBan = username;
    this.showBanModal = true;
  }

  makeAdmin(username: string) {
    if (this.community) {
      this.communityService
        .setAdmin(this.community.identifier, username)
        .subscribe({
          next: () => {
            this.openAlertModal(
              '¡Ahora ' + username + ' es administrador de esta comunidad!',
              () => {},
              false
            );
            this.getAdmin();
            this.reloadAllMembersList();
          },
          error: (r) => {
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Error haciendo administrador',
                description: r.error.message,
                code: 500,
              },
            });
          },
        });
    }
  }

  reloadAllMembersList() {
    this.moderators = [];
    this.moderatorsPage = 0;
    this.noMoreModerators = false;
    this.loadingMoreModerators = true;
    this.loadModerators();
    this.loadingMoreModerators = false;

    this.communityMembers = [];
    this.membersPage = 0;
    this.noMoreMembers = false;
    this.loadingMoreMembers = true;
    this.getMembers();
    this.loadingMoreMembers = false;

    this.getMembersCount();
  }

  addModeratorAction(username: string) {
    if (this.community) {
      this.communityService
        .addModerator(this.community.identifier, username)
        .subscribe({
          next: () => {
            this.openAlertModal(
              '¡' + username + ' es ahora moderador de esta comunidad!',
              () => {},
              false
            );
            this.reloadAllMembersList();
          },
          error: (r) => {
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Error añadiendo moderador',
                description: r.error.message,
                code: 500,
              },
            });
          },
        });
    }
  }

  addModerator(username: string) {
    if (this.community) {
      // if already a moderator, modal
      this.communityService
        .isModerator(this.community.identifier, username)
        .subscribe({
          next: (isModerator) => {
            if (isModerator) {
              this.openAlertModal(
                username + ' ya es moderador de esta comunidad',
                () => {},
                false
              );
            } else {
              this.addModeratorAction(username);
            }
          },
          error: (r) => {
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Error añadiendo moderador',
                description: r.error.message,
                code: 500,
              },
            });
          },
        });
    }
  }

  removeModerator(username: string) {
    if (this.community) {
      this.communityService
        .removeModerator(this.community.identifier, username)
        .subscribe({
          next: () => {
            this.openAlertModal(
              '¡' + username + ' ya no es moderador de esta comunidad!',
              () => {},
              false
            );
            this.reloadAllMembersList();
          },
          error: (r) => {
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Error eliminando moderador',
                description: r.error.message,
                code: 500,
              },
            });
          },
        });
    }
  }

  showDropdown(username: string): boolean {
    return (
      (this.isAdmin ||
        this.isModerator ||
        this.community?.admin.username === this.loggedUsername) &&
      (username !== this.community?.admin.username || this.isAdmin)
    );
  }

  canAddModerator(username: string) {
    return (
      (this.community?.admin.username === this.loggedUsername ||
        this.isAdmin) &&
      username !== this.community?.admin.username
    );
  }

  canRemoveModerator(username: string) {
    return (
      this.community?.admin.username === this.loggedUsername || this.isAdmin
    );
  }

  canMakeAdmin(username: string) {
    return (
      (this.community?.admin.username === this.loggedUsername ||
        this.isAdmin) &&
      username !== this.community?.admin.username
    );
  }

  canRemoveMember(username: string) {
    return (
      this.community?.admin.username === this.loggedUsername ||
      this.isAdmin ||
      this.isModerator
    );
  }

  canBanMember(username: string) {
    return (
      this.community?.admin.username === this.loggedUsername ||
      this.isAdmin ||
      this.isModerator
    );
  }

  setBanDuration(duration: string) {
    this.banDurationText = duration;
    switch (duration) {
      case 'day':
        this.banDurationTextTranslated = '1 día';
        break;
      case 'week':
        this.banDurationTextTranslated = '1 semana';
        break;
      case '2weeks':
        this.banDurationTextTranslated = '2 semanas';
        break;
      case 'month':
        this.banDurationTextTranslated = '1 mes';
        break;
      case '6months':
        this.banDurationTextTranslated = '6 meses';
        break;
      case 'forever':
        this.banDurationTextTranslated = 'Permanente';
        break;
      default:
        this.banDurationTextTranslated = '';
    }
  }

  toggleCommunityDescription() {
    this.showDescription = !this.showDescription;
  }

  deleteCommunity() {
    // open confirmation modal
    this.openAlertModal(
      '¿Estás seguro de que quieres eliminar esta comunidad? Esta acción no se puede deshacer.',
      () => {
        if (this.community) {
          this.communityService
            .deleteCommunity(this.community.identifier)
            .subscribe({
              next: () => {
                this.openAlertModal(
                  '¡La comunidad ha sido eliminada!',
                  () => {
                    window.location.href = '/';
                  },
                  false
                );
              },
            });
        }
      }
    );
  }

  clearMembersSearch() {
    this.membersSearchTerm = '';
    this.communityMembers = [];
    this.membersPage = 0;
    this.noMoreMembers = false;
    this.loadingMoreMembers = true;
    this.getMembers();
    this.loadingMoreMembers = false;
  }

  searchMembers() {
    if (!this.community) {
      return;
    }
    this.communityMembers = [];
    this.membersPage = 0;
    this.noMoreMembers = false;
    this.loadingMoreMembers = true;
    this.communityService
      .searchMembers(
        this.community.identifier,
        this.membersSearchTerm,
        this.membersPage,
        this.membersSize
      )
      .subscribe({
        next: (members) => {
          if (!members || members.length == 0) {
            this.noMoreMembers = true;
            this.loadingMoreMembers = false;
            return;
          }
          this.communityMembers = this.communityMembers.concat(members);
          this.loadingMoreMembers = false;
          this.membersPage += 1;
          this.noMoreMembers = members.length < this.membersSize;
        },
        error: (r) => {
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error buscando miembros',
              description: r.error.message,
              code: 500,
            },
          });
        },
      });
  }

  loadMorebansList() {
    this.loadingMoreBannedUsers = true;
    this.loadBannedUsers();
    this.loadingMoreBannedUsers = false;
  }

  openBanManagerModal() {
    this.showBanManagerModal = true;
    this.loadBannedUsers();
  }

  closeBanManagerModal() {
    this.showBanManagerModal = false;
  }

  loadBannedUsers() {
    if (this.community) {
      this.communityService
        .getBannedUsers(
          this.community.identifier,
          this.bannedUsersPage,
          this.bannedUsersSize
        )
        .subscribe({
          next: (bannedUsers) => {
            if (!bannedUsers || bannedUsers.length == 0) {
              this.noMoreBannedUsers = true;
              this.loadingMoreBannedUsers = false;
              return;
            }
            this.bannedUsers = this.bannedUsers.concat(bannedUsers);
            this.bannedUsersPage += 1;
            this.noMoreBannedUsers = bannedUsers.length < this.bannedUsersSize;
            this.loadingMoreBannedUsers = false;
          },
          error: (r) => {
            this.router.navigate(['/error'], {
              queryParams: {
                title: 'Error obteniendo usuarios baneados',
                description: r.error.message,
                code: 500,
              },
            });
          },
        });
    }
  }

  loadMoreBannedUsers() {
    this.loadingMoreBannedUsers = true;
    this.loadBannedUsers();
    this.loadingMoreBannedUsers = false;
  }

  openUnbanConfirmationModal(ban: Ban) {
    this.banToUnban = ban;
    this.showAlertModal = true;
    this.alertModalText = `¿Estás seguro de que quieres desbanear a ${ban.user.username}?`;
    this.confirmAction = () => this.unbanUser();
  }

  unbanUser() {
    if (this.banToUnban) {
      this.communityService.unbanUser(this.banToUnban.id).subscribe({
        next: () => {
          this.bannedUsers = this.bannedUsers.filter(
            (b) => b.id !== this.banToUnban!.id
          );
          this.banToUnban = undefined;
          this.showAlertModal = false;
        },
        error: (r) => {
          this.router.navigate(['/error'], {
            queryParams: {
              title: 'Error desbaneando usuario',
              description: r.error.message,
              code: 500,
            },
          });
        },
      });
    }
  }

  showBanInfoModal() {
    this.showUserBanInfo = true;
  }

  closeBanInfoModal() {
    this.showUserBanInfo = false;
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
