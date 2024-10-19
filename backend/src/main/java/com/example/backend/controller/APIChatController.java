package com.example.backend.controller;

import com.example.backend.entity.Chat;
import com.example.backend.entity.Message;
import com.example.backend.entity.User;
import com.example.backend.service.ChatService;
import com.example.backend.service.MessageService;
import com.example.backend.service.UserService;
import com.fasterxml.jackson.annotation.JsonView;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/v1")

public class APIChatController {
    private final ChatService chatService;
    private final MessageService messageService;
    private final UserService userService;

    interface ChatBasicInfo extends Chat.BasicInfo, User.UsernameInfo {
    }

    interface MessageBasicInfo extends Message.BasicInfo, User.UsernameInfo {
    }
    

    public APIChatController(ChatService chatService, MessageService messageService, UserService userService) {
        this.chatService = chatService;
        this.messageService = messageService;
        this.userService = userService;
    }

    @Operation(summary = "Create a new chat")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Chat created", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Chat.class)),
            }),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
    })
    @JsonView(ChatBasicInfo.class)
    @PostMapping("/chats")
    public ResponseEntity<Chat> createChat(HttpServletRequest request, @RequestBody Map<String, String> chatInfo) {
        String user1Username = chatInfo.get("user1");

        // is user logged in
        if (request.getUserPrincipal() == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String user2Username = chatInfo.get("user2");
        String chatName = chatInfo.get("name");

        User user1 = userService.getUserByUsername(user1Username);
        User user2 = userService.getUserByUsername(user2Username);

        if (user1 == null || user2 == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Chat chat = new Chat(user1, user2, chatName);
        chatService.saveChat(chat);

        return new ResponseEntity<>(chat, HttpStatus.CREATED);
    }

    @Operation(summary = "Get chat by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chat found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Chat.class)),
            }),
            @ApiResponse(responseCode = "404", description = "Chat not found"),
    })
    @JsonView(ChatBasicInfo.class)
    @GetMapping("/chats/{chatId}")
    public ResponseEntity<Chat> getChat(HttpServletRequest request, @PathVariable Long chatId) {

        // is user logged in
        if (request.getUserPrincipal() == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Chat chat = chatService.getChatById(chatId);
        if (chat == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // check that the user performing the request is one of the users in the chat or
        // a site admin
        String username = request.getUserPrincipal().getName();
        if (!chat.getUser1().getUsername().equals(username)
                && !chat.getUser2().getUsername().equals(username)
                && !userService.getUserByUsername(username).getRoles().contains("ADMIN")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(chat, HttpStatus.OK);
    }

    @Operation(summary = "Search chats by username or chat name (pageable)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chats found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Chat.class)),
            }),
            @ApiResponse(responseCode = "404", description = "Chats not found"),
    })
    @JsonView(ChatBasicInfo.class)
    @GetMapping("/users/{username}/chats")
    public ResponseEntity<List<Chat>> searchChats(HttpServletRequest request,
            @PathVariable(required = false) String username,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "true") boolean sorted) {

        // is user logged in
        if (request.getUserPrincipal() == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // check that the user performing the request is the user being searched for or a site admin
        String requesterUsername = request.getUserPrincipal().getName();
        if (!requesterUsername.equals(username) && !userService.getUserByUsername(requesterUsername).getRoles()
                .contains("ADMIN")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        List<Chat> userChats;

        PageRequest pageable = PageRequest.of(page, size);
        
        if (name != null) {
            userChats = chatService.getChatByName(name, username, sorted, pageable).getContent();
            return new ResponseEntity<>(userChats, HttpStatus.OK);
        } else {
            userChats = chatService.getChatByUser(username, sorted, pageable).getContent();
            return new ResponseEntity<>(userChats, HttpStatus.OK);
        }

    }

    @Operation(summary = "Get messages for a chat")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Messages retrieved", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class)),
            }),
            @ApiResponse(responseCode = "404", description = "Chat not found"),
    })
    @JsonView(MessageBasicInfo.class)
    @GetMapping("/chats/{chatId}/messages")
    public ResponseEntity<List<Message>> getMessages(HttpServletRequest request, @PathVariable Long chatId) {

        // is user logged in
        if (request.getUserPrincipal() == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Chat chat = chatService.getChatById(chatId);
        if (chat == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // check that the user performing the request is one of the users in the chat or
        // a site admin
        String username = request.getUserPrincipal().getName();
        if (!chat.getUser1().getUsername().equals(username)
                && !chat.getUser2().getUsername().equals(username)
                && !userService.getUserByUsername(username).getRoles().contains("ADMIN")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        List<Message> messages = chat.getMessages();
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    @Operation(summary = "Delete a chat")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Chat deleted"),
            @ApiResponse(responseCode = "404", description = "Chat not found"),
    })
    @DeleteMapping("/chats/{chatId}")
    public ResponseEntity<String> deleteChat(HttpServletRequest request, @PathVariable Long chatId) {
        Chat chat = chatService.getChatById(chatId);

        // is user logged in
        if (request.getUserPrincipal() == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if (chat == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // check that the user performing the request is one of the users in the chat or
        // a site admin
        String username = request.getUserPrincipal().getName();
        if (!chat.getUser1().getUsername().equals(username)
                && !chat.getUser2().getUsername().equals(username)
                && !userService.getUserByUsername(username).getRoles().contains("ADMIN")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        chatService.deleteChat(chat);
        return new ResponseEntity<>("Chat deleted", HttpStatus.OK);
    }

    @Operation(summary = "Modify chat name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chat name modified", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Chat.class)),
            }),
            @ApiResponse(responseCode = "404", description = "Chat not found"),
    })
    @JsonView(ChatBasicInfo.class)
    @PutMapping("/chats/{chatId}")
    public ResponseEntity<Chat> modifyChatName(HttpServletRequest request, @PathVariable Long chatId,
            @RequestParam String name) {
        Chat chat = chatService.getChatById(chatId);

        // is user logged in
        if (request.getUserPrincipal() == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if (chat == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // check that the user performing the request is one of the users in the chat or
        // a site admin
        String username = request.getUserPrincipal().getName();
        if (!chat.getUser1().getUsername().equals(username)
                && !chat.getUser2().getUsername().equals(username)
                && !userService.getUserByUsername(username).getRoles().contains("ADMIN")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        chat.setName(name);
        chatService.saveChat(chat);
        return new ResponseEntity<>(chat, HttpStatus.OK);
    }

}
