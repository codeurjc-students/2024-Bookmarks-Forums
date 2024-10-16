package com.example.backend.dto;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

public class PostDTO implements Serializable{

    private String title;

    private String content;

    private MultipartFile image;

    public PostDTO() {
    }

    public PostDTO(String title, String content, MultipartFile image) {
        this.title = title;
        this.content = content;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MultipartFile getImage() {
        return image;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }
    
}
