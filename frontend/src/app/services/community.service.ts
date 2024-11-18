import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Community } from '../models/community.model';
import { User } from '../models/user.model';
import { Post } from '../models/post.model';
import { CommunityStaff } from '../models/communityStaff.model';
import { Ban } from '../models/ban.model';

type CommunitiesMembersCount = [number, string, number];

const API_URL = '/api/v1/communities';

@Injectable({
  providedIn: 'root',
})
export class CommunityService {
  constructor(private readonly http: HttpClient) {}

  getCommunityById(communityId: number): Observable<Community> {
    return this.http
      .get<Community>(`${API_URL}/${communityId}`)
      .pipe(catchError((error) => throwError(() => error)));
  }

  searchCommunities(
    query: string,
    page: number,
    size: number,
    order: string
  ): Observable<Community[]> {
    let params = new HttpParams()
      .set('query', query)
      .set('page', page)
      .set('size', size)
      .set('order', order);
    return this.http
      .get<Community[]>(API_URL, { params: params })
      .pipe(catchError((error) => throwError(() => error)));
  }

  createCommunity(communityData: FormData): Observable<Community> {
    return this.http
      .post<Community>(API_URL, communityData)
      .pipe(catchError((error) => throwError(() => error)));
  }

  editCommunity(
    communityId: number,
    communityData: FormData,
    action: string
  ): Observable<Community> {
    let params = new HttpParams().set('action', action);
    return this.http
      .put<Community>(`${API_URL}/${communityId}`, communityData, {
        params: params,
      })
      .pipe(catchError((error) => throwError(() => error)));
  }

  deleteCommunity(communityId: number): Observable<string> {
    return this.http
      .delete(`${API_URL}/${communityId}`, { responseType: 'text' })
      .pipe(catchError((error) => throwError(() => error)));
  }

  getCommunityImage(communityId: number): Observable<Blob> {
    return this.http
      .get(`${API_URL}/${communityId}/pictures`, { responseType: 'blob' })
      .pipe(catchError((error) => throwError(() => error)));
  }

  getCommunityImageURL(communityId: number): string {
    return `${API_URL}/${communityId}/pictures`;
  }

  updateCommunityImage(
    communityId: number,
    image: File,
    action?: string
  ): Observable<Community> {
    const formData = new FormData();
    formData.append('image', image);
    let params = new HttpParams();
    if (action) {
      params = params.set('action', action);
    }
    return this.http
      .put<Community>(`${API_URL}/${communityId}/pictures`, formData, {
        params: params,
      })
      .pipe(catchError((error) => throwError(() => error)));
  }

  // This is also for the search feature
  getPosts(
    communityId: number,
    page: number,
    size: number,
    sort: string, /* 'lastModifiedDate' (default), 'creationDate', 'likes', 'replies' */
    query?: string
  ): Observable<Post[]> {
    let params = new HttpParams()
      .set('count', 'false')
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);
    if (query) {
      params = params.set('query', query);
    }
    return this.http
      .get<Post[]>(`${API_URL}/${communityId}/posts`, { params: params })
      .pipe(catchError((error) => throwError(() => error)));
  }

  getPostsCount(communityId: number): Observable<number> {
    let params = new HttpParams().set('count', 'true');
    return this.http
      .get<number>(`${API_URL}/${communityId}/posts`, { params: params })
      .pipe(catchError((error) => throwError(() => error)));
  }

  getMostPopularCommunitiesMembersCount(
    size: number
  ): Observable<CommunitiesMembersCount[]> {
    let params = new HttpParams().set('size', size);
    return this.http
      .get<CommunitiesMembersCount[]>(`${API_URL}/most-popular`, {
        params: params,
      })
      .pipe(catchError((error) => throwError(() => error)));
  }

  getMembersCount(communityID: number): Observable<number> {
    let params = new HttpParams().set('count', 'true');
    return this.http
      .get<number>(`${API_URL}/${communityID}/users`, { params: params })
      .pipe(catchError((error) => throwError(() => error)));
  }

  getMembers(
    communityID: number,
    page: number,
    size: number
  ): Observable<User[]> {
    let params = new HttpParams()
      .set('count', 'false')
      .set('page', page)
      .set('size', size);
    return this.http
      .get<User[]>(`${API_URL}/${communityID}/users`, { params: params })
      .pipe(catchError((error) => throwError(() => error)));
  }

  joinCommunity(
    communityID: number,
    username: string
  ): Observable<CommunityStaff> {
    let params = new HttpParams().set('action', 'join');
    return this.http
      .put<CommunityStaff>(
        `${API_URL}/${communityID}/users/${username}`,
        null,
        { params: params }
      )
      .pipe(catchError((error) => throwError(() => error)));
  }

  leaveCommunity(
    communityID: number,
    username: string
  ): Observable<CommunityStaff> {
    let params = new HttpParams().set('action', 'leave');
    return this.http
      .put<CommunityStaff>(
        `${API_URL}/${communityID}/users/${username}`,
        null,
        { params: params }
      )
      .pipe(catchError((error) => throwError(() => error)));
  }

  banUser(
    communityID: number,
    username: string,
    banReason: string,
    banUntil: string
  ): Observable<Ban> {
    let params = new HttpParams()
      .set('communityID', communityID)
      .set('username', username)
      .set('duration', banUntil)
      .set('reason', banReason);
    return this.http
      .post<Ban>(`${API_URL}/${communityID}/bans`, null, { params: params })
      .pipe(catchError((error) => throwError(() => error)));
  }

  unbanUser(banID: number): Observable<string> {
    return this.http
      .delete(`${API_URL}/bans/${banID}`, { responseType: 'text' })
      .pipe(catchError((error) => throwError(() => error)));
  }

  getBan(banID: number): Observable<Ban> {
    let params = new HttpParams().set('banInfo', 'raw');
    return this.http
      .get<Ban>(`${API_URL}/bans/${banID}`, { params: params })
      .pipe(catchError((error) => throwError(() => error)));
  }

  getBanStatus(banID: number): Observable<boolean> {
    let params = new HttpParams().set('banStatus', 'status');
    return this.http
      .get<boolean>(`${API_URL}/bans/${banID}`, { params: params })
      .pipe(catchError((error) => throwError(() => error)));
  }

  getAdmin(communityID: number): Observable<User> {
    return this.http
      .get<User>(`${API_URL}/${communityID}/admins`)
      .pipe(catchError((error) => throwError(() => error)));
  }

  /* Receives a JSON like:
    {
        "admin": username
    }
*/
  setAdmin(communityID: number, username: string): Observable<Community> {
    return this.http
      .put<Community>(`${API_URL}/${communityID}/admins`, { admin: username })
      .pipe(catchError((error) => throwError(() => error)));
  }

  getModerators(
    communityID: number,
    page: number,
    size: number
  ): Observable<User[]> {
    let params = new HttpParams().set('page', page).set('size', size);
    return this.http
      .get<User[]>(`${API_URL}/${communityID}/moderators`, { params: params })
      .pipe(catchError((error) => throwError(() => error)));
  }

  addModerator(
    communityID: number,
    username: string
  ): Observable<CommunityStaff> {
    let params = new HttpParams().set('action', 'add');
    return this.http
      .put<CommunityStaff>(
        `${API_URL}/${communityID}/moderators/${username}`,
        null,
        { params: params }
      )
      .pipe(catchError((error) => throwError(() => error)));
  }

  removeModerator(
    communityID: number,
    username: string
  ): Observable<CommunityStaff> {
    let params = new HttpParams().set('action', 'remove');
    return this.http
      .put<CommunityStaff>(
        `${API_URL}/${communityID}/moderators/${username}`,
        null,
        { params: params }
      )
      .pipe(catchError((error) => throwError(() => error)));
  }

  updateCommunityName(commID: number, newName: string): Observable<Community> {
    return this.http
      .put<Community>(`${API_URL}/${commID}`, { name: newName })
      .pipe(catchError((error) => throwError(() => error)));
  }

  updateCommunityDescription(
    commID: number,
    newDescription: string
  ): Observable<Community> {
    return this.http
      .put<Community>(`${API_URL}/${commID}`, { description: newDescription })
      .pipe(catchError((error) => throwError(() => error)));
  }

  updateCommunityBanner(
    communityId: number,
    image: File,
    action?: string
  ): Observable<Community> {
    const formData = new FormData();
    formData.append('file', image);
    let params = new HttpParams();
    if (action) {
      params = params.set('action', action);
    }
    return this.http
      .put<Community>(`${API_URL}/${communityId}/pictures`, formData, { params: params })
      .pipe(catchError((error) => throwError(() => error)));
  }

  getCommunities(query: string, page: number, size: number, sort: string, by: string): Observable<Community[]> {
    let params = new HttpParams()
      .set('query', query)
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort) // alphbetical, creationDate, lastPostDate, members
      .set('by', by); // name, description, admin, general (queryless, gets all communities), default
    return this.http
      .get<Community[]>(API_URL, { params: params })
      .pipe(catchError((error) => throwError(() => error)));
  }
}
