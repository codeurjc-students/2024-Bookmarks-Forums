import { User } from './user.model';
import { Message } from './message';

export interface ChatUser {
    username: string;
    roles?: string[];
}

export interface Chat {
    id: number;
    user1: ChatUser;
    user2: ChatUser;
    messages: Message[];
    lastMessageTime: Date;
    lastMessage?: string;
    lastMessageTimestamp?: Date;
    unreadCount: number;
} 