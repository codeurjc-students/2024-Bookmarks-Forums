package com.example.backend.service;

import com.example.backend.entity.Chat;
import com.example.backend.entity.Message;
import com.example.backend.repository.ChatRepository;
import com.example.backend.repository.MessageRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@DependsOn("userSampleService")
public class ChatSampleService {

    private final UserRepository userRepository;

    private final ChatRepository chatRepository;

    private final MessageRepository messageRepository;

    public ChatSampleService(UserRepository userRepository, ChatRepository chatRepository, MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
    }

    @PostConstruct
    public void init() {

        //CHATS

        /*
        Initializes two different chats, each between two users:
        1. Chat between BookReader_14 and AdminReader
        2. Chat between YourReader and BookReader_14
         */

        List<User> users = userRepository.findAll();
        List<Chat> chats = new ArrayList<>();

        Chat chat1 = new Chat(users.get(0), users.get(3), "Amazing chat between BookReader_14 and AdminReader");
        Chat chat2 = new Chat(users.get(2), users.get(0), null);

        chats.add(chat1);
        chats.add(chat2);

        chatRepository.saveAll(chats);

        // MESSAGES

        List<Message> messages = new ArrayList<>();

        Message message1 = new Message("Hello, AdminReader!", users.get(0), chat1);
        Message message2 = new Message("Hello, BookReader_14!", users.get(3), chat1);

        Message message3 = new Message("Hi, BookReader_14!", users.get(2), chat2);
        Message message4 = new Message("Hi, YourReader!", users.get(0), chat2);

        messages.add(message1);
        messages.add(message2);
        messages.add(message3);
        messages.add(message4);

        messageRepository.saveAll(messages);
    }
}
