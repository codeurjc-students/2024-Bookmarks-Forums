import { Community } from './community.model';
import { User } from './user.model';

export interface Post {
  identifier: number;
  hasImage?: boolean;
  author: User;
  title: string;
  content: string;
  community: Community;
  upvotes: number;
  downvotes: number;
  comments: number;
  creationDate?: string; // ISO string format
  creationTime?: string; // ISO string format
  fullCreationDate: string; // ISO string format
  lastReplyDate?: string; // ISO string format
  lastReplyTime?: string; // ISO string format
  fullLastReplyDate: string; // ISO string format
  isEdited?: boolean;
  lastEditDate?: string; // ISO string format
  lastEditTime?: string; // ISO string format
  fullLastEditDate: string; // ISO string format
}
