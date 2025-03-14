package com.example.backend.controller;

import com.example.backend.service.ChatService;
import com.example.backend.entity.Chat;
import com.example.backend.entity.Message;
import com.example.backend.entity.User;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/v1/chats")
public class APIChatController {

    private final ChatService chatService;

    public APIChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // Define a view that includes all necessary message fields
    interface ChatMessageView extends Message.BasicInfo, User.UsernameInfo {}
    interface ChatBasicView extends Chat.BasicInfo, ChatMessageView {}
    interface ChatListMessageView extends Message.BasicInfoForChatList, User.UsernameInfo {}
    interface ChatListBasicView extends Chat.BasicInfoForChatList, ChatListMessageView {}

    private String getCurrentUsername(HttpServletRequest request) {
        if (request.getUserPrincipal() == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        return request.getUserPrincipal().getName();
    }

    @JsonView(ChatListBasicView.class)
    @Operation(summary = "Get user's chats")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Chats found", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = Chat.class))
        }),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "404", description = "No chats found", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<Chat>> getUserChats(HttpServletRequest request, 
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String username = getCurrentUsername(request);
        System.out.println("Getting chats for authenticated user: " + username);
        Page<Chat> chatsPage = chatService.getUserChats(username, PageRequest.of(page, size));
        List<Chat> chats = chatsPage.getContent();
        System.out.println("Retrieved " + chats.size() + " chats");
        return ResponseEntity.ok(chats);
    }

    @JsonView(ChatMessageView.class)
    @Operation(summary = "Get messages from a specific chat")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Messages found", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class))
        }),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - User not part of this chat", content = @Content),
        @ApiResponse(responseCode = "404", description = "Chat not found", content = @Content)
    })
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<Message>> getChatMessages(HttpServletRequest request, @PathVariable Long chatId) {
        String username = getCurrentUsername(request);
        
        // Check if chat exists
        Chat chat = chatService.getChatById(chatId);
        if (chat == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Check if user is part of the chat
        if (!chat.getUser1().getUsername().equals(username) && 
            !chat.getUser2().getUsername().equals(username)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        List<Message> messages = chatService.getChatMessages(chatId, username);
        return ResponseEntity.ok(messages);
    }

    @Operation(summary = "Mark messages in a chat as read")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Messages marked as read"),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - User not part of this chat", content = @Content),
        @ApiResponse(responseCode = "404", description = "Chat not found", content = @Content)
    })
    @PostMapping("/{chatId}/read")
    public ResponseEntity<Void> markMessagesAsRead(HttpServletRequest request, @PathVariable Long chatId) {
        String username = getCurrentUsername(request);
        
        // Check if chat exists
        Chat chat = chatService.getChatById(chatId);
        if (chat == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Check if user is part of the chat
        if (!chat.getUser1().getUsername().equals(username) && 
            !chat.getUser2().getUsername().equals(username)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        chatService.markMessagesAsRead(chatId, username);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get count of unread messages")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Unread count retrieved", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class))
        }),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(HttpServletRequest request) {
        String username = getCurrentUsername(request);
        return ResponseEntity.ok(chatService.getUnreadMessageCount(username));
    }

    @Operation(summary = "Delete a chat")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Chat deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden - User not part of this chat", content = @Content),
        @ApiResponse(responseCode = "404", description = "Chat not found", content = @Content)
    })
    @DeleteMapping("/{chatId}")
    public ResponseEntity<Void> deleteChat(HttpServletRequest request, @PathVariable Long chatId) {
        String username = getCurrentUsername(request);
        
        // Check if chat exists
        Chat chat = chatService.getChatById(chatId);
        if (chat == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Check if user is part of the chat
        if (!chat.getUser1().getUsername().equals(username) && 
            !chat.getUser2().getUsername().equals(username)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        chatService.deleteChat(chatId, username);
        return ResponseEntity.ok().build();
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) {
            super(message);
        }
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> handleUnauthorized(UnauthorizedException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}