import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Chat } from '../models/chat.model';
import { Message } from '../models/message.model';

const API_URL = '/api/v1/chats';

@Injectable({
    providedIn: 'root',
})
export class ChatService {
    constructor(private readonly http: HttpClient) {}

    getChatById(chatId: number): Observable<Chat> {
        return this.http
            .get<Chat>(`${API_URL}/${chatId}`)
            .pipe(catchError((error) => throwError(() => error)));
    }

    getMessagesByChat(chatId: number, page: number, size: number): Observable<Map<string, Object>> {
        let params = new HttpParams()
            .set('page', page)
            .set('size', size);
        return this.http
            .get<Map<string, Object>>(`${API_URL}/${chatId}/messages`, { params: params })
            .pipe(catchError((error) => throwError(() => error)));
    }

    createChat(chatInfo: { user1: string; user2: string; name?: string }): Observable<Chat> {
        return this.http
            .post<Chat>(API_URL, chatInfo)
            .pipe(catchError((error) => throwError(() => error)));
    }

    modifyChat(chatId: number, action: string, name?: string): Observable<Chat> {
        let params = new HttpParams().set('action', action);
        if (name) {
            params = params.set('name', name);
        }
        return this.http
            .put<Chat>(`${API_URL}/${chatId}`, null, { params: params })
            .pipe(catchError((error) => throwError(() => error)));
    }

    deleteChat(chatId: number): Observable<any> {
        return this.http
            .delete(`${API_URL}/${chatId}`)
            .pipe(catchError((error) => throwError(() => error)));
    }

    sendMessage(type: string, messageDTO: FormData): Observable<Message> {
        let params = new HttpParams().set('type', type);
        return this.http
            .post<Message>(`${API_URL}/messages`, messageDTO, { params: params })
            .pipe(catchError((error) => throwError(() => error)));
    }

    getMessageById(messageId: number, info?: string): Observable<Message> {
        let params = new HttpParams();
        if (info) {
            params = params.set('info', info);
        }
        return this.http
            .get<Message>(`${API_URL}/messages/${messageId}`, { params: params })
            .pipe(catchError((error) => throwError(() => error)));
    }
}