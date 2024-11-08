import { User } from './user.model';

export interface Chat {
    identifier: number;
    name: string;
    user1: User;
    user2: User;
    creationDate?: string; // ISO date string
    creationTime?: string; // ISO time string
    fullCreationDate: string; // ISO date-time string
    lastMessageDate?: string; // ISO date string
    lastMessageTime?: string; // ISO time string
    fullLastMessageDate: string; // ISO date-time string
}