package com.gus.finsight.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserRegisterRequest {
    
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    @Pattern(regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ\\s'-]+$", message = "Nome não pode conter números ou símbolos")
    private String name;

    private String email;

    private String password;


    public UserRegisterRequest(){

    }

    public UserRegisterRequest(String name, String email, String password){

        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName(){
        return name;
    }

     public String getEmail(){
        return email;
    }

     public String getPassword(){
        return password;
    }

}