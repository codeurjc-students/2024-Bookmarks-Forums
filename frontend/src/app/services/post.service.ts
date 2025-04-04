import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Post } from '../models/post.model';
import { Reply } from '../models/reply.model';

const API_URL = '/api/v1/posts';
const COMMUNITY_API_URL = '/api/v1/communities';
const ME_API_URL = '/api/v1/users/me';
const USER_API_URL = '/api/v1/users';

@Injectable({
  providedIn: 'root',
})
export class PostService {
  constructor(private readonly http: HttpClient) {}

  getPostById(postId: number): Observable<Post> {
    return this.http
      .get<Post>(`${API_URL}/${postId}`)
      .pipe(catchError((error) => throwError(() => error)));
  }

  searchPosts(
    query: string,
    page: number,
    size: number,
    order: string
  ): Observable<Post[]> {
    let params = new HttpParams()
      .set('query', query)
      .set('page', page)
      .set('size', size)
      .set('order', order);
    return this.http
      .get<Post[]>(API_URL, { params: params })
      .pipe(catchError((error) => throwError(() => error)));
  }

  getCommunityPosts(
    communityID: number,
    count: boolean,
    page: number,
    size: number,
    sort: string,
    query?: string
  ): Observable<Map<string, Object>> {
    let params = new HttpParams()
      .set('count', count)
      .set('page', page)
      .set('size', size)
      .set('sort', sort);
    if (query) {
      params = params.set('query', query);
    }
    return this.http
      .get<Map<string, Object>>(`${COMMUNITY_API_URL}/${communityID}/posts`, {
        params: params,
      })
      .pipe(catchError((error) => throwError(() => error)));
  }

  createPost(communityID: number, postDTO: FormData): Observable<Post> {
    return this.http
      .post<Post>(`${COMMUNITY_API_URL}/${communityID}/posts`, postDTO)
      .pipe(catchError((error) => throwError(() => error)));
  }

  editPost(
    postId: number,
    postDTO: FormData,
    action: string
  ): Observable<Post> {
    let params = new HttpParams().set('action', action);
    return this.http
      .put<Post>(`${API_URL}/${postId}`, postDTO, { params: params })
      .pipe(catchError((error) => throwError(() => error)));
  }

  updatePostImage(
    postId: number,
    image: File,
    action?: string
  ): Observable<Post> {
    const formData = new FormData();
    formData.append('image', image);
    let params = new HttpParams();
    if (action) {
      params = params.set('action', action);
    }
    return this.http
      .put<Post>(`${API_URL}/${postId}/pictures`, formData, { params: params })
      .pipe(catchError((error) => throwError(() => error)));
  }

  deletePost(postId: number): Observable<string> {
    return this.http
      .delete(`${API_URL}/${postId}`, { responseType: 'text' })
      .pipe(catchError((error) => throwError(() => error)));
  }

  getPostImage(postId: number): Observable<Blob> {
    return this.http
      .get(`${API_URL}/${postId}/pictures`, { responseType: 'blob' })
      .pipe(catchError((error) => throwError(() => error)));
  }

  getPostImageURL(postId: number): string {
    return '/api/v1/posts/' + postId + '/pictures';
  }

  getRepliesOfPost(
    postId: number,
    page: number,
    size: number,
    order: string
  ): Observable<Reply[]> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('order', order);
    return this.http
      .get<Reply[]>(`${API_URL}/${postId}/replies/all`, { params: params })
      .pipe(catchError((error) => throwError(() => error)));
  }

  getReplyById(replyId: number): Observable<any> {
    return this.http
      .get<any>(`/api/v1/replies/${replyId}`)
      .pipe(catchError((error) => throwError(() => error)));
  }

  searchReplies(
    postId: number,
    criteria: string,
    query: string,
    page: number,
    size: number
  ): Observable<Reply[]> {
    let params = new HttpParams()
      .set('criteria', criteria)
      .set('query', query)
      .set('page', page)
      .set('size', size);
    return this.http
      .get<Reply[]>(`/api/v1/posts/${postId}/replies`, { params: params })
      .pipe(catchError((error) => throwError(() => error)));
  }

  createReply(
    postId: number,
    replyData: { [key: string]: string }
  ): Observable<any> {
    return this.http
      .post<any>(`${API_URL}/${postId}/replies`, replyData)
      .pipe(catchError((error) => throwError(() => error)));
  }

  likeReply(replyId: number, action: string): Observable<any> {
    let params = new HttpParams().set('action', action);
    return this.http
      .put<any>(`/api/v1/replies/${replyId}`, null, { params: params })
      .pipe(catchError((error) => throwError(() => error)));
  }

  deleteReply(replyId: number): Observable<any> {
    return this.http
      .delete(`/api/v1/replies/${replyId}`, { responseType: 'text' }) // Expect a text response
      .pipe(catchError((error) => throwError(() => error)));
  }

  /*
        option: 'most-liked-users': get most liked (popular) posts from users that the logged in user follows (default)
                'most-liked-communities': get most liked (popular) posts from communities that the logged in user follows
                'most-recent-communities': get most recent posts from communities that the logged in user follows
    */
  getUserRecommendations(option: string, page: number, size: number) {
    let params = new HttpParams().set('page', page).set('size', size);
    switch (option) {
      case 'most-liked-communities':
        return this.http
          .get<Post[]>(`${ME_API_URL}/communities/posts/most-liked`, {
            params: params,
          })
          .pipe(catchError((error) => throwError(() => error)));
      case 'most-recent-communities':
        return this.http
          .get<Post[]>(`${ME_API_URL}/communities/posts/most-recent`, {
            params: params,
          })
          .pipe(catchError((error) => throwError(() => error)));
      default:
        return this.http
          .get<Post[]>(`${ME_API_URL}/following/posts/most-liked`, {
            params: params,
          })
          .pipe(catchError((error) => throwError(() => error)));
    }
  }

  /*
        option: 'most-liked-users': get most liked (popular) posts from most followed users (default)
                'most-liked-communities': get most liked (popular) posts from most followed communities
                'most-recent-communities': get most recent posts from most followed communities
    */
  getGeneralRecommendations(option: string, page: number, size: number) {
    let params = new HttpParams().set('page', page).set('size', size);
    switch (option) {
      case 'most-liked-communities':
        return this.http
          .get<Post[]>(`${COMMUNITY_API_URL}/most-popular/posts/most-liked`, {
            params: params,
          })
          .pipe(catchError((error) => throwError(() => error)));
      case 'most-recent-communities':
        return this.http
          .get<Post[]>(`${COMMUNITY_API_URL}/most-popular/posts/most-recent`, {
            params: params,
          })
          .pipe(catchError((error) => throwError(() => error)));
      default:
        return this.http
          .get<Post[]>(`${USER_API_URL}/posts/most-liked`, { params: params })
          .pipe(catchError((error) => throwError(() => error)));
    }
  }

  hasUserVoted(postId: number, username: string, type: string) {
    let params = new HttpParams().set('username', username).set('type', type);
    return this.http
      .get<boolean>(`${API_URL}/${postId}/votes`, { params: params })
      .pipe(catchError((error) => throwError(() => error)));
  }

  hasUserLikedReply(replyId: number, username: string) {
    let params = new HttpParams().set('username', username);
    return this.http
      .get<boolean>(`/api/v1/replies/${replyId}/votes`, { params: params })
      .pipe(catchError((error) => throwError(() => error)));
  }

  deletePostImage(postId: number): Observable<string> {
    return this.http
      .delete(`${API_URL}/${postId}/pictures`, { responseType: 'text' })
      .pipe(catchError((error) => throwError(() => error)));
  }

  getPostsOfUser(
    username: string,
    page: number,
    size: number,
    order: string,
    query?: string
  ): Observable<Post[]> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('order', order);
    if (query && query.length > 0) {
      params = params.set('query', query);
    }
    return this.http
      .get<Post[]>(`${USER_API_URL}/${username}/posts`, { params: params })
      .pipe(catchError((error) => throwError(() => error)));
  }

  getPostCountOfUser(username: string): Observable<number> {
    return this.http
      .get<number>(`${USER_API_URL}/${username}/posts/count`)
      .pipe(catchError((error) => throwError(() => error)));
  }
}
