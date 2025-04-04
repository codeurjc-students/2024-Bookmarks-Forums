import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ChatService } from '../../services/chat.service';
import { LoginService } from '../../services/session.service';
import { Page } from '../../models/page';
import { Chat, ChatUser } from '../../models/chat';
import { Message } from '../../models/message';
import { UserService } from '../../services/user.service';
import { TitleService } from '../../services/title.service';
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
  showChatList: boolean = false;

  // Current chat state
  currentChat: Chat | null = null;
  messages: Message[] = [];
  newMessage: string = '';
  loadingMessages: boolean = false;
  loadingTemporaryChat: boolean = false;

  // User state
  loggedUsername: string = '';
  userLoaded: boolean = false;
  recipientUsername: string | null = null;

  // Alert modal state
  showAlertModal: boolean = false;
  alertModalText: string = '';
  confirmAction: () => void = () => {};

  constructor(
    private chatService: ChatService,
    private loginService: LoginService,
    private userService: UserService,
    private route: ActivatedRoute,
    private router: Router,
    private titleService: TitleService
  ) {}

  ngOnInit() {
    this.titleService.setTitle('Chat');
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

    // Load initial chats and handle direct chat opening
    if (this.recipientUsername) {
      const username = this.recipientUsername;
      this.loadingChats = true;
      this.chatService.getChats(0, this.chatsSize).subscribe({
        next: (response: Chat[]) => {
          this.chats = response;
          this.noMoreChats = response.length < this.chatsSize;
          this.chatsPage = 1;
          
          // Check for existing chat with the recipient
          const existingChat = this.chats.find(chat => 
            this.getOtherUser(chat) === username
          );

          if (existingChat) {
            // If chat exists, open it
            this.openChat(existingChat);
          } else {
            // If no chat exists, create a temporary one
            this.openOrCreateChat(username);
          }
          
          this.loadingChats = false;
        },
        error: (error) => {
          this.handleError('Error al cargar los chats', error);
          this.loadingChats = false;
          // Still create temporary chat in case of error
          this.openOrCreateChat(username);
        }
      });
    } else {
      // If no recipient specified, just load the chat list
      this.loadChats();
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
    const otherUsername = this.getOtherUser(chat);
    this.titleService.setTitle(`Chat con ${otherUsername}`);
    this.loadMessages(chat.id);
    // Hide chat list on mobile when opening a chat
    if (window.innerWidth <= 991) {
      this.showChatList = false;
    }
    this.chatService.markMessagesAsRead(chat.id).subscribe({
      next: () => {
        // Update local unread count after marking messages as read
        if (chat.unreadCount > 0) {
          chat.unreadCount = 0;
        }
      },
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
      this.titleService.setTitle(`Chat con ${username}`);
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

    // If this is a temporary chat, update the chat list after a short delay
    if (this.currentChat.id === 0) {
      this.loadingTemporaryChat = true;
      setTimeout(() => {
        this.chatsPage = 0;
        this.loadingChats = true;
        
        this.chatService.getChats(0, this.chatsSize).subscribe({
          next: (response: Chat[]) => {
            const newChat = response.find(chat => 
              chat.user1.username === recipientUsername || chat.user2.username === recipientUsername
            );
            
            if (newChat) {
              // Update chat list
              this.chats = response;
              this.noMoreChats = response.length < this.chatsSize;
              
              // Update current chat
              this.currentChat = newChat;
              this.loadMessages(newChat.id);
            }
            
            this.loadingChats = false;
            this.loadingTemporaryChat = false;
            this.chatsPage = 1; // Set to 1 since we've loaded the first page
          },
          error: (error) => {
            this.handleError('Error al cargar el chat nuevo', error);
            this.loadingChats = false;
            this.loadingTemporaryChat = false;
          }
        });
      }, 500); // Small delay to ensure backend has processed the message
    } else {
      // For existing chats, update the chat list immediately
      const chatIndex = this.chats.findIndex(c => c.id === this.currentChat!.id);
      if (chatIndex > -1) {
        const chat = this.chats[chatIndex];
        chat.messages = [tempMessage, ...chat.messages];
        chat.lastMessage = tempMessage;
        chat.lastMessageTime = tempMessage.timestamp;

        // Move chat to top of the list
        this.chats.splice(chatIndex, 1);
        this.chats.unshift(chat);
      }
    }
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
        chat.lastMessage = message;  // Update lastMessage property
        chat.lastMessageTime = message.timestamp;

        // Update unread count if message is not from current user and chat is not currently open
        if (message.sender.username !== this.loggedUsername && 
            (!this.currentChat || this.currentChat.id !== chat.id)) {
          chat.unreadCount = (chat.unreadCount || 0) + 1;
        }

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
      // New chat was created, reload chats and update current chat
      this.chatsPage = 0;
      this.loadingChats = true;
      
      // Get fresh chat list and update current chat in a single operation
      this.chatService.getChats(0, this.chatsSize).subscribe({
        next: (response: Chat[]) => {
          const newChat = response.find(chat => 
            chat.user1.username === otherUsername || chat.user2.username === otherUsername
          );
          
          if (newChat) {
            // Update chat list
            this.chats = response;
            this.noMoreChats = response.length < this.chatsSize;
            
            // Update current chat
            this.currentChat = newChat;
            this.loadMessages(newChat.id);
          }
          
          this.loadingChats = false;
          this.chatsPage = 1; // Set to 1 since we've loaded the first page
        },
        error: (error) => {
          this.handleError('Error al cargar el chat nuevo', error);
          this.loadingChats = false;
        }
      });
    } else if (!existingChat && !this.currentChat) {
      // If we don't have the chat in our list and no chat is open, reload the list
      // This handles receiving a message for a new chat when no chat is open
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

  openAlertModal(text: string, action: () => void) {
    this.alertModalText = text;
    this.confirmAction = action;
    this.showAlertModal = true;
  }

  closeAlertModal() {
    this.showAlertModal = false;
  }

  deleteChat(chatId: number) {
    this.openAlertModal(
      '¿Seguro que quieres eliminar este chat? Esta acción no se puede deshacer.',
      () => {
        this.chatService.deleteChat(chatId).subscribe({
          next: () => {
            // Remove chat from list
            this.chats = this.chats.filter(chat => chat.id !== chatId);
            
            // If the deleted chat was the current chat, clear it and reset title
            if (this.currentChat?.id === chatId) {
              this.currentChat = null;
              this.messages = [];
              this.titleService.setTitle('Chat');
            }
          },
          error: (error) => this.handleError('Error al eliminar el chat', error)
        });
      }
    );
  }

  // Profile picture handling
  profilePicture(username: string | undefined): string {
    if (!username) return '';
    return this.userService.getPostImageURL(username);
  }

  // Add toggle method for chat list
  toggleChatList() {
    this.showChatList = !this.showChatList;
  }
} 