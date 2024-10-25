package com.example.backend.dto;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

public class MessageDTO implements Serializable {
    
        private String content;
    
        private String sender;

        private Long chatId;

        private MultipartFile image;

        public MessageDTO() {
        }

        public MessageDTO(String content, String sender, Long chatId, MultipartFile image) {
            this.content = content;
            this.sender = sender;
            this.chatId = chatId;
            this.image = image;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }

        public Long getChatId() {
            return chatId;
        }

        public void setChatId(Long chatId) {
            this.chatId = chatId;
        }

        public MultipartFile getImage() {
            return image;
        }

        public void setImage(MultipartFile image) {
            this.image = image;
        }
    
}
