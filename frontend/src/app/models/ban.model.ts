import { User } from './user.model';
import { Community } from './community.model';

export interface Ban {
    id: number;
    user: User;
    community: Community;
    banReason: string;
    banUntil: Date;
}