import { Chat } from './chat.model';
import { User } from './user.model';

export interface Message {
  identifier: number;
  content: string;
  sender: User;
  chat: Chat;
  creationDate: string; // ISO date string
  creationTime: string; // ISO time string
  fullCreationDate: string; // ISO date-time string
}
