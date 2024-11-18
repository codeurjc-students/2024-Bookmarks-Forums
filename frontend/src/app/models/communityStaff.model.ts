import { User } from "./user.model";

export interface CommunityStaff {
    moderators?: User[];
    administrators?: User[];
    members?: User[];
    bannedUsers?: User[];
}
