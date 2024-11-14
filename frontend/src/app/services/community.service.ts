import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Community } from '../models/community.model';

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

    searchCommunities(query: string, page: number, size: number, order: string): Observable<Map<string, Object>> {
        let params = new HttpParams()
            .set('query', query)
            .set('page', page)
            .set('size', size)
            .set('order', order);
        return this.http
            .get<Map<string, Object>>(API_URL, { params: params })
            .pipe(catchError((error) => throwError(() => error)));
    }

    createCommunity(communityData: FormData): Observable<Community> {
        return this.http
            .post<Community>(API_URL, communityData)
            .pipe(catchError((error) => throwError(() => error)));
    }

    editCommunity(communityId: number, communityData: FormData, action: string): Observable<Community> {
        let params = new HttpParams().set('action', action);
        return this.http
            .put<Community>(`${API_URL}/${communityId}`, communityData, { params: params })
            .pipe(catchError((error) => throwError(() => error)));
    }   

    deleteCommunity(communityId: number): Observable<any> {
        return this.http
            .delete(`${API_URL}/${communityId}`)
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

    updateCommunityImage(communityId: number, image: File, action?: string): Observable<Community> {
        const formData = new FormData();
        formData.append('image', image);
        let params = new HttpParams();
        if (action) {
            params = params.set('action', action);
        }
        return this.http
            .put<Community>(`${API_URL}/${communityId}/pictures`, formData, { params: params })
            .pipe(catchError((error) => throwError(() => error)));
    }   

    getCommunityPosts(communityId: number, page: number, size: number, sort: string, query?: string): Observable<Map<string, Object>> {
        let params = new HttpParams()
            .set('page', page)
            .set('size', size)
            .set('sort', sort);
        if (query) {
            params = params.set('query', query);
        }
        return this.http
            .get<Map<string, Object>>(`${API_URL}/${communityId}/posts`, { params: params })
            .pipe(catchError((error) => throwError(() => error)));
    }
    
    getMostPopularCommunitiesMembersCount(size: number): Observable<CommunitiesMembersCount[]> {
        let params = new HttpParams().set('size', size);
        return this.http
            .get<CommunitiesMembersCount[]>(`${API_URL}/most-popular`, { params: params })
            .pipe(catchError((error) => throwError(() => error)));
    }

}