import { Community } from "./community.model";

export interface User {
  username: string;
  roles: string[];
  alias: string;
  description: string;
  email: string;
  pfpString?: string;
  followers: number;
  following: number;
  fullCreationDate: string;
  creationDate?: string;
  creationTime?: string;
  banCount: number;
  isDisabled: boolean;
  disabledUntil: string;
  communities: Community[];
}
