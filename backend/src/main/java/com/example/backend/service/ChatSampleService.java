package com.example.backend.service;

import com.example.backend.entity.Chat;
import com.example.backend.entity.Message;
import com.example.backend.entity.User;
import com.example.backend.repository.ChatRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@DependsOn("userSampleService")
public class ChatSampleService {

    private final ChatRepository chatRepository;
    private final UserService userService;

    public ChatSampleService(ChatRepository chatRepository, UserService userService) {
        this.chatRepository = chatRepository;
        this.userService = userService;
    }

    @PostConstruct
    public void init() {
        // Get users
        User bookReader = userService.getUserByUsername("BookReader_14");
        User adminReader = userService.getUserByUsername("AdminReader");
        User fanBook = userService.getUserByUsername("FanBook_785");
        User yourReader = userService.getUserByUsername("YourReader");

        // Create chats
        List<Chat> chats = new ArrayList<>();

        // Chat between BookReader_14 and AdminReader (ID: 1)
        Chat chat1 = new Chat(bookReader, adminReader);
        chats.add(chat1);

        // Chat between BookReader_14 and FanBook_785 (ID: 2)
        Chat chat2 = new Chat(bookReader, fanBook);
        chats.add(chat2);

        // Chat between BookReader_14 and YourReader (ID: 3)
        Chat chat3 = new Chat(bookReader, yourReader);
        chats.add(chat3);

        // Chat between AdminReader and FanBook_785 (ID: 4)
        Chat chat4 = new Chat(adminReader, fanBook);
        chats.add(chat4);

        chatRepository.saveAll(chats);

        // Messages for the chats
        LocalDateTime now = LocalDateTime.now();

        // Messages for chat1 (BookReader_14 and AdminReader)
        Message msg1 = new Message(bookReader, adminReader, "Hello Admin!");
        msg1.setTimestamp(now.minusHours(2));
        msg1.setRead(true);
        chat1.addMessage(msg1);

        Message msg2 = new Message(adminReader, bookReader, "Hi BookReader!");
        msg2.setTimestamp(now.minusHours(1));
        msg2.setRead(true);
        chat1.addMessage(msg2);

        Message msg3 = new Message(bookReader, adminReader, "How are you?");
        msg3.setTimestamp(now.minusMinutes(30));
        msg3.setRead(false); // Unread message
        chat1.addMessage(msg3);

        // Messages for chat2 (BookReader_14 and FanBook_785)
        Message msg4 = new Message(fanBook, bookReader, "Hey BookReader!");
        msg4.setTimestamp(now.minusDays(1));
        msg4.setRead(true);
        chat2.addMessage(msg4);

        Message msg5 = new Message(bookReader, fanBook, "Hi FanBook!");
        msg5.setTimestamp(now.minusHours(12));
        msg5.setRead(true);
        chat2.addMessage(msg5);

        Message msg6 = new Message(fanBook, bookReader, "What are you reading?");
        msg6.setTimestamp(now.minusMinutes(15));
        msg6.setRead(false); // Unread message
        chat2.addMessage(msg6);

        // Messages for chat3 (BookReader_14 and YourReader)
        Message msg7 = new Message(yourReader, bookReader, "Hello there!");
        msg7.setTimestamp(now.minusDays(2));
        msg7.setRead(true);
        chat3.addMessage(msg7);

        Message msg8 = new Message(bookReader, yourReader, "Hi YourReader!");
        msg8.setTimestamp(now.minusDays(1));
        msg8.setRead(true);
        chat3.addMessage(msg8);

        Message msg9 = new Message(yourReader, bookReader, "Want to discuss books?");
        msg9.setTimestamp(now.minusMinutes(5));
        msg9.setRead(false); // Unread message
        chat3.addMessage(msg9);

        // Messages for chat4 (AdminReader and FanBook_785)
        Message msg10 = new Message(adminReader, fanBook, "Hey FanBook, how's it going?");
        msg10.setTimestamp(now.minusHours(3));
        msg10.setRead(true);
        chat4.addMessage(msg10);

        Message msg11 = new Message(fanBook, adminReader, "Hi Admin! Everything's great!");
        msg11.setTimestamp(now.minusHours(2));
        msg11.setRead(true);
        chat4.addMessage(msg11);

        Message msg12 = new Message(adminReader, fanBook, "Any new book recommendations?");
        msg12.setTimestamp(now.minusMinutes(20));
        msg12.setRead(false); // Unread message
        chat4.addMessage(msg12);

        chatRepository.saveAll(chats);
    }
} 