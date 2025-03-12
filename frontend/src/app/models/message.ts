export interface MessageUser {
    username: string;
}

export interface Message {
    id?: number;
    sender: MessageUser;
    receiver: MessageUser;
    content: string;
    timestamp: Date;
    read: boolean;
} 