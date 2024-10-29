import { Component } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'Bookmarks Forums';

  showNavbarFooter: boolean = true;

  constructor(private readonly router: Router, private readonly activatedRoute: ActivatedRoute) {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      // Hides the navbar and footer on the login and signup pages
      this.showNavbarFooter = !(this.activatedRoute.firstChild?.snapshot.routeConfig?.path === 'login' ||
        this.activatedRoute.firstChild?.snapshot.routeConfig?.path === 'signup');
    });
  }
}
