package com.example.backend.controller;

import com.example.backend.dto.MessageDTO;
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

import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Map;

import javax.imageio.ImageIO;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.Blob;
import java.util.List;

@RestController
@RequestMapping("/api/v1")

public class APIChatController {
    private final ChatService chatService;
    private final MessageService messageService;
    private final UserService userService;

    interface ChatBasicInfo extends Chat.BasicInfo, User.UsernameInfo {
    }

    interface MessageBasicInfo extends Message.BasicInfo, User.UsernameInfo, Chat.idInfo {
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
        String user2Username = chatInfo.get("user2");
        String chatName = chatInfo.get("name");

        // is user logged in
        if (request.getUserPrincipal() == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // is the user performing the request one of the users in the chat or a site admin?
        if (!request.getUserPrincipal().getName().equals(user1Username)
                && !request.getUserPrincipal().getName().equals(user2Username)
                && !userService.getUserByUsername(request.getUserPrincipal().getName()).getRoles().contains("ADMIN")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

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

        // check that the user performing the request is the user being searched for or
        // a site admin
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

        // Delete the chat's messages
        for (Message message : chat.getMessages()) {
            messageService.deleteMessage(message);
        }

        chatService.deleteChat(chat);
        return new ResponseEntity<>("Chat deleted", HttpStatus.OK);
    }

    @Operation(summary = "Modify chat (action = edit, leave)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chat name modified", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Chat.class)),
            }),
            @ApiResponse(responseCode = "404", description = "Chat not found"),
    })
    @JsonView(ChatBasicInfo.class)
    @PutMapping("/chats/{chatId}")
    public ResponseEntity<Chat> modifyChatName(HttpServletRequest request, @PathVariable Long chatId,
            @RequestParam(required = false) String name, @RequestParam String action) {
        Chat chat = chatService.getChatById(chatId);

        // is user logged in
        if (request.getUserPrincipal() == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if (action == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
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

        if (action.equals("edit")) {
            if (name != null) {
                chat.setName(name);
                chatService.saveChat(chat);
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } else if (action.equals("leave")) {
            chatService.leaveChat(chat, username);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(chat, HttpStatus.OK);

    }

    // MESSAGES ----------------------------------------------------------------

    // Get message by ID
    @Operation(summary = "Get message by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class)),
            }),
            @ApiResponse(responseCode = "404", description = "Message not found"),
    })
    @JsonView(MessageBasicInfo.class)
    @GetMapping("/messages/{messageId}")
    public ResponseEntity<Object> getMessage(HttpServletRequest request, @PathVariable Long messageId, @RequestParam(required = false) String info) {

        /*
         * info = true: return message ExtraInfo (won't return image)
         * info = false: return message BasicInfo
         */

        // is user logged in
        if (request.getUserPrincipal() == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Message message = messageService.getMessageById(messageId);
        if (message == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // check that the user performing the request is one of the users in the chat or
        // a site admin
        String username = request.getUserPrincipal().getName();
        if (!message.getSender().getUsername().equals(username)
                && !message.getChat().getUser1().getUsername().equals(username)
                && !message.getChat().getUser2().getUsername().equals(username)
                && !userService.getUserByUsername(username).getRoles().contains("ADMIN")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if (info != null && info.equals("true")) {
            return new ResponseEntity<>(message, HttpStatus.OK);
        }

        // if the message has an image, return the image
        Blob image = message.getImage();
        if (image != null) {
            try {
                int blobLength = (int) image.length();
                byte[] blobAsBytes = image.getBytes(1, blobLength);
                return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(blobAsBytes);
            } catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>(message, HttpStatus.OK);
        }

    }

    // Delete message
    @Operation(summary = "Delete message")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Message deleted"),
            @ApiResponse(responseCode = "404", description = "Message not found"),
    })
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<String> deleteMessage(HttpServletRequest request, @PathVariable Long messageId) {
        Message message = messageService.getMessageById(messageId);

        // is user logged in
        if (request.getUserPrincipal() == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if (message == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // check that the user performing the request is the sender of the message or
        // a site admin
        String username = request.getUserPrincipal().getName();
        if (!message.getSender().getUsername().equals(username)
                && !userService.getUserByUsername(username).getRoles().contains("ADMIN")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        messageService.deleteMessage(message);
        return new ResponseEntity<>("Message deleted", HttpStatus.OK);
    }

    // Get messages by sender username
    @Operation(summary = "Get messages by sender username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Messages found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class)),
            }),
            @ApiResponse(responseCode = "404", description = "Messages not found"),
    })
    @JsonView(MessageBasicInfo.class)
    @GetMapping("/users/{username}/messages")
    public ResponseEntity<List<Message>> getMessagesBySenderUsername(HttpServletRequest request,
            @PathVariable String username, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // is user logged in and the person performing the request is the user being
        // searched for or a site admin
        if (!request.getUserPrincipal().getName().equals(username)
                && !userService.getUserByUsername(request.getUserPrincipal().getName()).getRoles().contains("ADMIN")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        List<Message> messages = messageService.getMessagesBySenderUsername(username, PageRequest.of(page, size))
                .getContent();
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    // Get messages by chat
    @Operation(summary = "Get messages by chat")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Messages found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class)),
            }),
            @ApiResponse(responseCode = "404", description = "Messages not found"),
    })
    @JsonView(MessageBasicInfo.class)
    @GetMapping("/chats/{chatId}/messages")
    public ResponseEntity<List<Message>> getMessagesByChat(HttpServletRequest request, @PathVariable Long chatId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

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

        List<Message> messages = messageService.getMessagesByChat(chatId, PageRequest.of(page, size)).getContent();
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    // Send message
    @Operation(summary = "Send message (type = text, image)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Message sent", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class)),
            }),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
    })
    @JsonView(MessageBasicInfo.class)
    @PostMapping("/messages")
    public ResponseEntity<Message> sendMessage(HttpServletRequest request, @RequestParam String type,
            @ModelAttribute MessageDTO messageDTO) {

        // is user logged in
        if (request.getUserPrincipal() == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if (type == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (type.equals("text")) {
            return sendTextMessage(request, messageDTO);
        } else if (type.equals("image")) {
            return sendImageMessage(request, messageDTO);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    private ResponseEntity<Message> sendTextMessage(HttpServletRequest request, MessageDTO messageDTO) {
        String senderUsername = messageDTO.getSender();
        Long chatId = messageDTO.getChatId();
        String content = messageDTO.getContent();

        // is user logged in and the person performing the request is the sender of the
        // message or a site admin
        if (!request.getUserPrincipal().getName().equals(senderUsername)
                && !userService.getUserByUsername(request.getUserPrincipal().getName()).getRoles().contains("ADMIN")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        User sender = userService.getUserByUsername(senderUsername);
        Chat chat = chatService.getChatById(chatId);

        if (sender == null || chat == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // check that the user performing the request is one of the users in the chat
        if (!chat.getUser1().getUsername().equals(senderUsername)
                && !chat.getUser2().getUsername().equals(senderUsername)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Message message = new Message(content, sender, chat);
        messageService.saveMessage(message);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(message.getIdentifier())
                .toUri();
        return ResponseEntity.created(location).body(message);
    }

    private ResponseEntity<Message> sendImageMessage(HttpServletRequest request, MessageDTO messageInfo) {

        if (messageInfo.getImage() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        String senderUsername = messageInfo.getSender();
        Long chatId = messageInfo.getChatId();

        // is user logged in and the person performing the request is the sender of the
        // message or a site admin
        if (!request.getUserPrincipal().getName().equals(senderUsername)
                && !userService.getUserByUsername(request.getUserPrincipal().getName()).getRoles().contains("ADMIN")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        User sender = userService.getUserByUsername(senderUsername);
        Chat chat = chatService.getChatById(chatId);

        // check that the user performing the request is one of the users in the chat
        if (!chat.getUser1().getUsername().equals(senderUsername)
                && !chat.getUser2().getUsername().equals(senderUsername)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if (sender == null || chat == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Message message = new Message(null, sender, chat);

        MultipartFile image = messageInfo.getImage();

        try (InputStream is = image.getInputStream()) {
            try {
                ImageIO.read(is).toString();
                long size = image.getSize() / 1024 / 1024;
                if (size > 5) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
                message.setImage(BlobProxy.generateProxy(image.getInputStream(), image.getSize()));
            } catch (IOException e) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        messageService.saveMessage(message);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(message.getIdentifier())
                .toUri();
        return ResponseEntity.created(location).body(message);
    }

}
