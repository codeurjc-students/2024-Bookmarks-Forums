import { Injectable } from '@angular/core';
import { Title } from '@angular/platform-browser';

@Injectable({
  providedIn: 'root'
})
export class TitleService {
  private readonly baseTitle = 'Bookmarks Forums';

  constructor(private titleService: Title) {}

  /**
   * Sets the page title
   * @param pageTitle - The title of the current page
   */
  setTitle(pageTitle?: string) {
    if (pageTitle) {
      this.titleService.setTitle(`${pageTitle} | ${this.baseTitle}`);
    } else {
      this.titleService.setTitle(this.baseTitle);
    }
  }
} 