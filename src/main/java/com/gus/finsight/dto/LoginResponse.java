package com.gus.finsight.dto;

public class LoginResponse {

    private String name;
    private String token;

    protected LoginResponse(){

    }

    public LoginResponse(String name, String token){

        this.name = name;
        this.token = token;

    }

    public String getName(){
        return name;
    }

    public String getToken(){
        return token;
    }
    
}
