package com.gus.finsight.dto;

public class UserLoginRequest {

    private String email;
    private String password;

    protected UserLoginRequest(){

    }

    public UserLoginRequest(String email, String password){
        this.email = email;
        this.password = password;

    }

    public String getPassword(){
        return password;
    }

    public String getEmail(){
        return email;
    }
    
}
