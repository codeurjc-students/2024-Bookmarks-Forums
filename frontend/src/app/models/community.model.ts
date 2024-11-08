import { User } from './user.model';

export interface Community {
    identifier: number;
    name: string;
    description: string;
    admin: User;
    bannerString: string;
    creationDate: Date;
    creationTime: string;
    fullCreationDate: Date;
    lastPostDate: Date;
    lastPostTime: string;
    fullLastPostDate: Date;
}