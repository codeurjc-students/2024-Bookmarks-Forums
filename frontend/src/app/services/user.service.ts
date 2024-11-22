import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { User } from '../models/user.model';
import { Community } from '../models/community.model';

type UsersLikesCount = [string, number];

const API_URL = '/api/v1/users';

const baseUrl = '/api/v1';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  constructor(private readonly http: HttpClient) {}

  getUser(username: string): Observable<User> {
    return this.http
      .get<User>(API_URL + '/' + username)
      .pipe(catchError((error) => throwError(() => error)));
  }

  searchUsers(
    username: string,
    page: number,
    size: number,
    orderByCreationDate: boolean
  ): Observable<Map<string, Object>> {
    let params = new HttpParams()
      .set('query', username)
      .set('page', page)
      .set('size', size)
      .set('orderByCreationDate', orderByCreationDate);
    return this.http
      .get<Map<string, Object>>(API_URL, { params: params })
      .pipe(catchError((error) => throwError(() => error)));
  }

  downloadProfilePicture(username: string): Observable<Blob> {
    return this.http
      .get(API_URL + '/' + username + '/pictures', { responseType: 'blob' })
      .pipe(catchError((error) => throwError(() => error)));
  }

  getPostImageURL(username: string): string {
    return API_URL + '/' + username + '/pictures';
  }

  getCurrentUser(): Observable<User> {
    return this.http
      .get<User>(baseUrl + '/users/me')
      .pipe(catchError((error) => throwError(() => error)));
  }

  isUsernameTaken(username: string): Observable<boolean> {
    return this.http
      .get<boolean>(API_URL + '/' + username + '/taken')
      .pipe(catchError((error) => throwError(() => error)));
  }

  getFollowers(
    username: string,
    page: number,
    size: number
  ): Observable<User[]> {
    let params = new HttpParams().set('page', page).set('size', size);
    return this.http
      .get<User[]>(API_URL + '/' + username + '/followers', {
        params: params,
      })
      .pipe(catchError((error) => throwError(() => error)));
  }

  getFollowing(
    username: string,
    page: number,
    size: number
  ): Observable<User[]> {
    let params = new HttpParams().set('page', page).set('size', size);
    return this.http
      .get<User[]>(API_URL + '/' + username + '/following', {
        params: params,
      })
      .pipe(catchError((error) => throwError(() => error)));
  }

  getUserCommunities(
    username: string,
    admin: boolean,
    page: number,
    size: number
  ): Observable<Community[]> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('admin', admin);
    return this.http
      .get<Community[]>(API_URL + '/' + username + '/communities', {
        params: params,
      })
      .pipe(catchError((error) => throwError(() => error)));
  }

  getUserCommunitiesCount(
    username: string,
    admin: boolean
  ): Observable<number> {
    let params = new HttpParams().set('admin', admin);
    return this.http
      .get<number>(API_URL + '/' + username + '/communities/count', {
        params: params,
      })
      .pipe(catchError((error) => throwError(() => error)));
  }

  getUserPostsCount(username: string): Observable<number> {
    return this.http
      .get<number>(API_URL + '/' + username + '/posts/count')
      .pipe(catchError((error) => throwError(() => error)));
  }

  signup(userInfo: { username: string; alias: string; email: string; password: string }): Observable<User> {
    const formData = new FormData();
    formData.append('username', userInfo.username);
    formData.append('alias', userInfo.alias);
    formData.append('email', userInfo.email);
    formData.append('password', userInfo.password);
  
    return this.http
      .post<User>(API_URL, formData)
      .pipe(catchError((error) => throwError(() => error)));
  }

  editUser(
    username: string,
    userInfo: {
      alias?: string;
      email?: string;
      description?: string;
      password?: string;
    }
  ): Observable<User> {
    const params = new HttpParams().set('action', 'edit');
    return this.http
      .put<User>(`${API_URL}/${username}`, userInfo, { params })
      .pipe(catchError((error) => throwError(() => error)));
  }

  followUser(username: string, otherUsername: string): Observable<User> {
    const params = new HttpParams().set('action', 'follow').set('otherUsername', otherUsername);
    return this.http
      .put<User>(API_URL + '/' + username, null, { params: params })
      .pipe(catchError((error) => throwError(() => error)));
  }

  unfollowUser(username: string, otherUsername: string): Observable<User> {
    const params = new HttpParams().set('action', 'unfollow').set('otherUsername', otherUsername);
    return this.http
      .put<User>(API_URL + '/' + username, null, { params: params })
      .pipe(catchError((error) => throwError(() => error)));
  }

  deleteUser(username: string): Observable<string> {
    return this.http
      .delete(API_URL + '/' + username, { responseType: 'text' })
      .pipe(catchError((error) => throwError(() => error)));
  }

  getFollowersCount(username: string): Observable<number> {
    return this.getUser(username).pipe(
      map((user: User) => user.followers),
      catchError((error) => throwError(() => error))
    );
  }

  getFollowingCount(username: string): Observable<number> {
    return this.getUser(username).pipe(
      map((user: User) => user.following),
      catchError((error) => throwError(() => error))
    );
  }

  uploadProfilePicture(username: string, file: File): Observable<User> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http
      .put<User>(API_URL + '/' + username + '/pictures', formData)
      .pipe(catchError((error) => throwError(() => error)));
  }

  getMostPopularUsersLikesCount(size: number): Observable<UsersLikesCount[]> {
    let params = new HttpParams().set('size', size);
    return this.http
      .get<UsersLikesCount[]>(API_URL + '/most-popular', { params: params })
      .pipe(catchError((error) => throwError(() => error)));
  }

  isUserFollowing(username: string, otherUsername: string): Observable<boolean> {
    return this.http
      .get<boolean>(API_URL + '/' + username + '/following/' + otherUsername)
      .pipe(catchError((error) => throwError(() => error)));
  }
}
