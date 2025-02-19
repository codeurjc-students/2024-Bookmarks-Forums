import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { UserLogin } from '../models/userLogin.model';
import { User } from '../models/user.model';

@Injectable({
  providedIn: 'root',
})
export class LoginService {
  private readonly baseUrl = '/api/v1/';
  private readonly userUrl = '/api/v1/users/';

  isLogged: boolean = false;
  user: User | undefined;

  constructor(private readonly httpClient: HttpClient) {}

  checkLogged(): Observable<boolean> {
    return this.httpClient
      .get<User>(this.userUrl + 'me', { withCredentials: true })
      .pipe(
        map(() => {
          this.isLogged = true;
          return true;
        }),
        catchError((error) => {
          if (error.status === 401) {
            this.isLogged = false;
            this.user = undefined;
            return of(false);
          } else {
            return throwError(
              () =>
                new Error(
                  'Server error (' +
                    error.status +
                    '): ' +
                    error.statusText +
                    ')'
                )
            );
          }
        })
      );
  }

  login(user: UserLogin) {
    return this.httpClient
      .post(this.baseUrl + 'login', user, { withCredentials: true })
      .pipe(catchError((error) => throwError(() => error)));
  }

  logout(): Observable<any> {
    return this.httpClient
      .post(this.baseUrl + 'logout', { withCredentials: true })
      .pipe(catchError((error) => this.handleError(error)));
  }

  isUserLogged(): boolean {
    return this.isLogged;
  }

  getLoggedUser() {
    return this.httpClient
      .get<User>(this.userUrl + 'me', { withCredentials: true })
      .pipe(catchError((error) => this.handleError(error)));
  }

  getLoggedUsername(): string {
    return this.user?.username ?? '';
  }

  isAdmin(): boolean {
    if (this.user) {
      return this.user.roles.includes('ADMIN');
    }
    return false;
  }

  checkAdmin(): Observable<boolean> {
    return this.httpClient
      .get<User>(this.userUrl + 'me', { withCredentials: true })
      .pipe(
        map((user) => {
          this.user = user;
          return user.roles.includes('ADMIN');
        }),
        catchError((error) => {
          if (error.status === 401) {
            this.isLogged = false;
            this.user = undefined;
            return of(false);
          } else {
            return throwError(
              () =>
                new Error(
                  'Server error (' +
                    error.status +
                    '): ' +
                    error.statusText +
                    ')'
                )
            );
          }
        })
      );
  }

  //Error handlers

  private handleError(error: any) {
    console.log('[!] ERROR: ');
    console.error(error);
    return throwError(
      () =>
        new Error(
          'Server error (' + error.status + '): ' + error.statusText + ')'
        )
    );
  }
}
