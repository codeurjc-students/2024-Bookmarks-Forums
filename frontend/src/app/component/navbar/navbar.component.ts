import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { LoginService } from '../../services/session.service';
import { UserService } from '../../services/user.service';
import { ChatService } from '../../services/chat.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css'],
})
export class NavbarComponent implements OnInit, OnDestroy {
  page = 0;
  userSearch = false;
  loggedIn = false;
  username = '';
  query: string = '';

  loadedUser = false;

  isAdmin = false;

  unreadMessages: number = 0;
  private unreadCountSubscription?: Subscription;

  constructor(
    private router: Router,
    public userService: UserService,
    private sessionService: LoginService,
    private activatedRoute: ActivatedRoute,
    private chatService: ChatService
  ) {
    this.activatedRoute.params.subscribe((params) => {
      if (this.router.url.includes('search')) {
        localStorage.setItem(
          'userSearch',
          params['users'] === 'true' ? 'true' : 'false'
        );
      }
    });
  }

  onKeyDown(event: any) {
    if (event.key === 'Enter') {
      this.search(event.target.value);
    }
  }

  search(query: string) {
    this.query = query;

    this.router.navigate(['/search'], {
      queryParams: { users: this.userSearch, query: this.query },
    });
  }

  profileImage(username: string) {
    return this.userService.getPostImageURL(username);
  }

  defaultImage() {
    return 'assets/defaultProfilePicture.png';
  }

  toggleSearch(type: string) {
    let checkbox = document.getElementById('search-select');
    if (checkbox) {
      this.userSearch = type === 'users';
      checkbox.setAttribute('checked', this.userSearch ? 'true' : 'false');
    }
  }

  ngOnInit() {
    this.sessionService.checkLogged().subscribe({
      next: (r) => {
        this.loggedIn = r;
        if (this.loggedIn) {
          this.sessionService.getLoggedUser().subscribe({
            next: (r) => {
              this.loadedUser = true;
              this.username = r.username;

              if (r.roles.includes('ADMIN')) {
                this.isAdmin = true;
              }

              // Subscribe to unread count updates
              this.unreadCountSubscription = this.chatService.getUnreadCount()
                .subscribe(count => {
                  this.unreadMessages = count;
                });
            },
            error: (r) => {
              console.error('Error: ' + JSON.stringify(r));
            },
          });
        }
      },
    });

    let checkbox = document.getElementById('search-select') as HTMLInputElement;
    if (checkbox) {
      setTimeout(() => {
        let currentState = checkbox.getAttribute('ng-reflect-model');

        if (currentState == null) {
          currentState = checkbox.checked.toString();
        }

        if (currentState != localStorage.getItem('userSearch')) {
          checkbox.click();
        }
      }, 1);

      checkbox.onchange = () => {
        localStorage.setItem('userSearch', this.userSearch ? 'true' : 'false');
      };
    }

    window.addEventListener('scroll', this.onScroll);
  }

  ngOnDestroy() {
    // Clean up subscription
    if (this.unreadCountSubscription) {
      this.unreadCountSubscription.unsubscribe();
    }
    window.removeEventListener('scroll', this.onScroll);
  }

  onScroll = () => {
    const gradientOverlay = document.querySelector('.gradient-overlay') as HTMLElement;
    const navbar = document.querySelector('.bm-navbar') as HTMLElement;
    const scrollTop = window.scrollY;

    if (scrollTop > navbar.offsetHeight) {
      gradientOverlay.style.opacity = '1';
    } else {
      gradientOverlay.style.opacity = '0';
    }
  };

  logout() {
    this.sessionService.logout().subscribe({
      next: (r) => {
        this.loadedUser = false;
        this.loggedIn = false;

        // reload the page
        window.location.reload();
      },
      error: (r) => {
        console.error('Error: ' + JSON.stringify(r));
      },
    });
  }
}
