import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ChatService } from '../../services/chat.service';
import { LoginService } from '../../services/session.service';
import { Page } from '../../models/page';
import { Chat, ChatUser } from '../../models/chat';
import { Message } from '../../models/message';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent implements OnInit, OnDestroy {
  // Chat list state
  chats: Chat[] = [];
  chatsPage: number = 0;
  chatsSize: number = 10;
  loadingChats: boolean = false;
  noMoreChats: boolean = false;

  // Current chat state
  currentChat: Chat | null = null;
  messages: Message[] = [];
  newMessage: string = '';
  loadingMessages: boolean = false;

  // User state
  loggedUsername: string = '';
  userLoaded: boolean = false;
  recipientUsername: string | null = null;

  constructor(
    private chatService: ChatService,
    private loginService: LoginService,
    private userService: UserService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    this.checkIfLoggedIn();

    // Check for direct chat parameter
    this.route.queryParams.subscribe(params => {
      if (params['username']) {
        this.recipientUsername = params['username'];
      }
    });
  }

  ngOnDestroy() {
    this.chatService.disconnect();
  }

  checkIfLoggedIn() {
    this.loginService.checkLogged().subscribe({
      next: (isLogged) => {
        if (isLogged) {
          this.loginService.getLoggedUser().subscribe({
            next: (user) => {
              this.userLoaded = true;
              this.loggedUsername = user.username;
              this.initializeChat();
            },
            error: (error) => this.handleError('Error al obtener el usuario logueado', error)
          });
        } else {
          this.router.navigate(['/login']);
        }
      },
      error: (error) => {
        if (error.status !== 401) {
          this.handleError('Error al comprobar el estado de la sesión', error);
        } else {
          this.router.navigate(['/login']);
        }
      }
    });
  }

  initializeChat() {
    // Connect WebSocket
    this.chatService.connect(this.loggedUsername);

    // Subscribe to new messages
    this.chatService.getMessages().subscribe({
      next: (message) => {
        // If we have a current chat open and the message belongs to it
        if (this.currentChat && this.currentChat.id !== 0) {
          const otherUsername = this.getOtherUser(this.currentChat);
          if (message.sender.username === otherUsername || 
              message.sender.username === this.loggedUsername) {
            this.messages.push(message);
            this.scrollToBottom();
          }
        }
        
        // Update chat list regardless of current chat
        this.updateChatList(message);
      },
      error: (error) => this.handleError('Error al recibir mensajes', error)
    });

    // Load initial chats
    this.loadChats();

    // Only open direct chat if username provided
    if (this.recipientUsername) {
      this.openOrCreateChat(this.recipientUsername);
    } else {
      this.currentChat = null;
      this.messages = [];
    }
  }

  loadChats() {
    if (this.loadingChats || this.noMoreChats) return;
    
    this.loadingChats = true;
    this.chatService.getChats(this.chatsPage, this.chatsSize).subscribe({
      next: (response: Chat[]) => {
        if (!response || response.length === 0) {
          this.noMoreChats = true;
          this.loadingChats = false;
          return;
        }

        if (this.chatsPage === 0) {
          this.chats = response;
        } else {
          this.chats = [...this.chats, ...response];
        }

        this.chatsPage++;
        this.noMoreChats = response.length < this.chatsSize;
        this.loadingChats = false;
      },
      error: (error) => {
        this.handleError('Error al cargar los chats', error);
        this.loadingChats = false;
      }
    });
  }

  loadMoreChats() {
    this.loadChats();
  }

  openChat(chat: Chat) {
    this.currentChat = chat;
    this.loadMessages(chat.id);
    this.chatService.markMessagesAsRead(chat.id).subscribe({
      error: (error) => this.handleError('Error al marcar mensajes como leídos', error)
    });
  }

  openOrCreateChat(username: string) {
    const existingChat = this.chats.find(chat => 
      chat.user1.username === username || chat.user2.username === username
    );

    if (existingChat) {
      this.openChat(existingChat);
    } else {
      this.currentChat = {
        id: 0,
        user1: { username: this.loggedUsername } as ChatUser,
        user2: { username } as ChatUser,
        messages: [],
        lastMessageTime: new Date(),
        unreadCount: 0
      };
      this.messages = [];
    }
  }

  loadMessages(chatId: number) {
    this.loadingMessages = true;
    this.chatService.getChatMessages(chatId).subscribe({
      next: (messages: Message[]) => {
        // Sort messages from oldest to newest
        this.messages = messages.sort((a, b) => 
          new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime()
        );
        this.loadingMessages = false;
        this.scrollToBottom();
      },
      error: (error) => {
        this.handleError('Error al cargar los mensajes', error);
        this.loadingMessages = false;
      }
    });
  }

  sendMessage() {
    if (!this.newMessage.trim() || !this.currentChat) return;

    const recipientUsername = this.getOtherUser(this.currentChat);
    const content = this.newMessage.trim();
    
    // Create a temporary message object
    const tempMessage: Message = {
      id: 0,
      sender: { username: this.loggedUsername },
      receiver: { username: recipientUsername },
      content: content,
      timestamp: new Date(),
      read: false
    };

    // Add message to the list
    this.messages.push(tempMessage);
    this.scrollToBottom();
    
    // Clear input
    this.newMessage = '';
    
    // Send message through WebSocket
    this.chatService.sendMessage(recipientUsername, content);
  }

  getOtherUser(chat: Chat): string {
    return chat.user1.username === this.loggedUsername
      ? chat.user2.username
      : chat.user1.username;
  }

  isOwnMessage(message: Message): boolean {
    return message.sender.username === this.loggedUsername;
  }

  updateChatList(message: Message) {
    // Find existing chat with the other user
    const otherUsername = message.sender.username === this.loggedUsername 
      ? message.receiver.username 
      : message.sender.username;
    
    const existingChat = this.chats.find(chat => 
      this.getOtherUser(chat) === otherUsername
    );

    if (existingChat) {
      // Update existing chat
      const chatIndex = this.chats.findIndex(c => c.id === existingChat.id);
      if (chatIndex > -1) {
        // Move chat to top
        const chat = this.chats[chatIndex];
        chat.messages = [message, ...chat.messages];
        chat.lastMessageTime = message.timestamp;
        this.chats.splice(chatIndex, 1);
        this.chats.unshift(chat);

        // If this is the current chat, update its reference and sort messages
        if (this.currentChat && this.currentChat.id === chat.id) {
          this.currentChat = chat;
          // Ensure messages remain sorted by timestamp
          this.messages = [...this.messages].sort((a, b) => 
            new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime()
          );
        }
      }
    } else if (this.currentChat?.id === 0 && 
              (otherUsername === this.getOtherUser(this.currentChat))) {
      // New chat was created
      this.chatsPage = 0;
      this.loadChats();
    }
  }

  scrollToBottom() {
    setTimeout(() => {
      const messagesContainer = document.querySelector('.messages-container');
      if (messagesContainer) {
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
      }
    }, 100);
  }

  handleError(title: string, error: any) {
    this.router.navigate(['/error'], {
      queryParams: {
        title,
        description: error.error?.message || error.message,
        code: error.status
      }
    });
  }

  // Profile picture handling
  profilePicture(username: string | undefined): string {
    if (!username) return '';
    return this.userService.getPostImageURL(username);
  }
} 