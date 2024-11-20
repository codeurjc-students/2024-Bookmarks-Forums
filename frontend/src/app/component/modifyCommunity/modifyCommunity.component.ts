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
  selector: 'app-modify-community',
  templateUrl: './modifyCommunity.component.html',
  styleUrls: ['./modifyCommunity.component.css', '../../../animations.css'],
  providers: [DatePipe],
})
export class ModifyCommunityComponent implements OnInit {
  showModal: boolean = false;

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

  showAlertModal: boolean = false;
  alertModalText: string = '';
  confirmAction: () => void = () => {};
  showCancelButton: boolean = true;

  showDescription: boolean = false;

  // COMMUNITY EDITOR
  communityName: string = '';
  communityDescription: string = '';
  communityNameMaxLength = 50;
  wantsToDeleteBanner: boolean = false;

  selectedImageURL: string | ArrayBuffer | null = null;

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
    this.loadCommunity();
  }

  loadUserData(user: User) {
    this.user = user;
    this.loggedUsername = user.username;
    this.isAdmin = user.roles.includes('ADMIN');
    this.isUserMember();
  }

  getAdmin() {
    if (this.community) {
      this.communityService.getAdmin(this.community.identifier).subscribe({
        next: (admin) => {
          this.admin = admin;
        },
        error: (r) => {
          console.error('Error getting community admin: ' + JSON.stringify(r));
        },
      });
    }
  }

  loadCommunity() {
    let communityID = Number(this.route.snapshot.paramMap.get('identifier'));
    this.communityService.getCommunityById(communityID).subscribe({
      next: (community) => {
        this.community = community;
        this.communityName = community.name;
        this.communityDescription = community.description;
        this.getMembersCount();
        this.getAdmin();
        this.getPostsCount();
      },
      error: (r) => {
        console.error('Error getting community: ' + JSON.stringify(r));
      },
    });
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
            console.error(
              'Error checking if user is moderator: ' + JSON.stringify(r)
            );
          },
        });
    }
  }

  isUserMember() {
    if (this.community) {
      this.communityService
        .isUserMember(this.community.identifier, this.loggedUsername)
        .subscribe({
          next: (isMember) => {
            this.isMember = isMember;
            this.isCommunityAdmin =
              this.community?.admin.username === this.loggedUsername;
            this.checkModerator();
          },
          error: (r) => {
            // if the error is unauthorized, the user is not a member
            if (r.status == 401) {
              this.isMember = false;
            } else {
              console.error(
                'Error checking if user is member: ' + JSON.stringify(r)
              );
            }
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

  getMembersCount() {
    if (this.community) {
      this.communityService
        .getMembersCount(this.community.identifier)
        .subscribe({
          next: (count) => {
            this.communityMembersCount = count;
          },
          error: (r) => {
            console.error(
              'Error getting community members count: ' + JSON.stringify(r)
            );
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
          console.error(
            'Error getting community posts count: ' + JSON.stringify(r)
          );
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

  toggleCommunityDescription() {
    this.showDescription = !this.showDescription;
  }

  editCommunity() {
    if (this.community) {
      // goes to community/<id>/edit
      window.location.href =
        '/community/' + this.community.identifier + '/edit';
    }
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

  deleteBanner() {
    if (this.community) {
      if (!this.community.hasBanner && this.selectedImageURL) {
        this.selectedImageURL = null;
      } else {
        this.openAlertModal(
          '¿Estás seguro de que quieres eliminar la imagen de la comunidad?',
          () => {
            this.wantsToDeleteBanner = true;
            this.selectedImageURL = null;
          },
          true
        );
      }
    }
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
  }

  adjustTextareaHeight(event: Event): void {
    const textarea = event.target as HTMLTextAreaElement;
    const scrollTop = window.scrollY || document.documentElement.scrollTop;
    textarea.style.height = 'auto';
    textarea.style.height = `${textarea.scrollHeight}px`;
    window.scrollTo(0, scrollTop);
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
        this.communityService.updateCommunityBanner(
          this.community.identifier,
          file,
          undefined
        ).subscribe({
          next: () => {
            this.showDoneModal();
          },
          error: (r) => {
            console.error(
              'Error updating community banner: ' + JSON.stringify(r)
            );
          },
        });
      }
    } else {
      this.showDoneModal();
    }
  }

  showDoneModal(){
    this.openAlertModal(
      '¡Los cambios han sido guardados!',
      () => {
        window.location.href =
          '/community/' + this.community?.identifier;
      },
      false
    );
  }

  manageBanner(){
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
            console.error(
              'Error deleting community banner: ' + JSON.stringify(r)
            );
          },
        });
    } else {
      this.setBanner();
    }
  }

  confirmChanges() {
    if (this.community) {
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

      this.communityService
        .editCommunity(this.community.identifier, communityData, 'edit')
        .subscribe({
          next: () => {
            this.manageBanner();
            
          },
          error: (r) => {
            console.error('Error editing community: ' + JSON.stringify(r));
          },
        });
    }
  }
}
