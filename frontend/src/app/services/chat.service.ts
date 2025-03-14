import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { filter, map, scan } from 'rxjs/operators';
import { Message } from '../models/message';
import { Chat } from '../models/chat';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private API_URL = '/api/v1/chats';
  private webSocket: WebSocket | null = null;
  private messageSubject = new Subject<Message>();
  private unreadCountSubject = new Subject<number>();
  private currentUsername: string = '';

  constructor(private http: HttpClient) {}

  connect(username: string): void {
    if (this.webSocket && this.webSocket.readyState === WebSocket.OPEN) {
      console.log('WebSocket already connected');
      return;
    }

    this.currentUsername = username;
    const wsUrl = `/ws/chat?username=${username}`;
    console.log('Connecting to WebSocket:', wsUrl);
    
    this.webSocket = new WebSocket(wsUrl);

    this.webSocket.onopen = () => {
      console.log('WebSocket connection established');
    };

    this.webSocket.onmessage = (event) => {
      console.log('Received message:', event.data);
      try {
        const message = JSON.parse(event.data);
        // Add sender and receiver objects to match Message interface
        const formattedMessage: Message = {
          id: message.id,
          sender: { username: message.senderUsername },
          receiver: { username: message.recipientUsername },
          content: message.content,
          timestamp: new Date(message.timestamp),
          read: false
        };
        this.messageSubject.next(formattedMessage);
      } catch (error) {
        console.error('Error parsing message:', error);
      }
    };

    this.webSocket.onclose = (event) => {
      console.log('WebSocket connection closed. Code:', event.code, 'Reason:', event.reason, 'Clean:', event.wasClean);
      if (!event.wasClean) {
        console.log('Attempting to reconnect in 5 seconds...');
        setTimeout(() => this.connect(username), 5000);
      }
    };

    this.webSocket.onerror = (error) => {
      console.error('WebSocket error:', error);
    };
  }

  disconnect(): void {
    if (this.webSocket) {
      this.webSocket.close();
      this.webSocket = null;
    }
  }

  sendMessage(recipientUsername: string, content: string): void {
    if (this.webSocket && this.webSocket.readyState === WebSocket.OPEN) {
      const message = {
        senderUsername: this.currentUsername,
        recipientUsername,
        content,
        timestamp: Date.now()
      };
      console.log('Sending message:', message);
      this.webSocket.send(JSON.stringify(message));
    } else {
      console.error('WebSocket is not connected. State:', this.webSocket?.readyState);
    }
  }

  getChats(page: number = 0, size: number = 10): Observable<Chat[]> {
    return this.http.get(`${this.API_URL}?page=${page}&size=${size}`, { responseType: 'text' })
      .pipe(
        map(response => {
          console.log('Raw response:', response);
          try {
            // Fix the malformed roles array in the JSON string
            const fixedResponse = response.replace(/"roles"\s*:\s*\[\s*\]/g, '"roles": []');
            const parsedResponse = JSON.parse(fixedResponse);
            console.log('Parsed response:', parsedResponse);

            // Ensure each chat has an unreadCount property
            return parsedResponse.map((chat: Chat) => ({
              ...chat,
              unreadCount: chat.unreadCount || 0,
              messages: chat.messages || [],
              lastMessageTime: chat.lastMessageTime ? new Date(chat.lastMessageTime) : new Date()
            }));
          } catch (e) {
            console.error('Error parsing chats response:', e);
            console.error('Response that failed to parse:', response);
            return [];
          }
        })
      );
  }

  getChatMessages(chatId: number, page: number = 0, size: number = 20): Observable<Message[]> {
    return this.http.get(`${this.API_URL}/${chatId}/messages?page=${page}&size=${size}`, { responseType: 'text' })
      .pipe(
        map(response => {
          try {
            const fixedResponse = response.replace(/"roles"\s*:\s*\[\s*\]/g, '"roles": []');
            const parsedResponse = JSON.parse(fixedResponse);
            return parsedResponse;
          } catch (e) {
            console.error('Error parsing messages response:', e);
            return [];
          }
        })
      );
  }

  markMessagesAsRead(chatId: number): Observable<void> {
    return new Observable(observer => {
      this.http.post<void>(`${this.API_URL}/${chatId}/read`, {}).subscribe({
        next: () => {
          // After marking messages as read, get updated unread count
          this.http.get<number>(`${this.API_URL}/unread-count`).subscribe(count => {
            this.unreadCountSubject.next(count);
            observer.next();
            observer.complete();
          });
        },
        error: (error) => observer.error(error)
      });
    });
  }

  getUnreadCount(): Observable<number> {
    // Initial load of unread count
    this.http.get<number>(`${this.API_URL}/unread-count`).subscribe(
      count => this.unreadCountSubject.next(count)
    );
    
    // Return observable that combines initial count and updates
    return this.unreadCountSubject.asObservable();
  }

  getMessages(): Observable<Message> {
    return this.messageSubject.asObservable();
  }

  deleteChat(chatId: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${chatId}`)
      .pipe(
        map(() => {
          // After deleting the chat, get updated unread count
          this.http.get<number>(`${this.API_URL}/unread-count`).subscribe(
            count => this.unreadCountSubject.next(count)
          );
        })
      );
  }
} 