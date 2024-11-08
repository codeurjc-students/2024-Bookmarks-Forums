import { User } from './user.model';
import { Post } from './post.model';

export interface Reply {
  identifier: number;
  title: string;
  content: string;
  likes: number;
  author: User;
  post: Post;
  creationDate?: string; // ISO date string
  creationTime?: string; // ISO time string
  fullCreationDate: string; // ISO datetime string
}
