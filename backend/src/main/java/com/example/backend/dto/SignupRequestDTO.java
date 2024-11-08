package com.example.backend.dto;

import java.io.Serializable;

public class SignupRequestDTO implements Serializable{

    private String username;
    private String email;
    private String alias;
    private String password;

    public SignupRequestDTO() {
    }

    public SignupRequestDTO(String username, String email, String alias, String password) {
        this.username = username;
        this.email = email;
        this.alias = alias;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    
}
