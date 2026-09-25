package com.gus.finsight.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.gus.finsight.repository.UserRepository;
import com.gus.finsight.dto.UserLoginRequest;
import com.gus.finsight.dto.UserRegisterRequest;
import com.gus.finsight.entity.User;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(UserRegisterRequest request) {

    String senhaCriptografada = passwordEncoder.encode(request.getPassword());

        User newUser = new User(
            request.getName(),
            request.getEmail(),
            senhaCriptografada
        );
        
        return userRepository.save(newUser);
    }


   public User login(UserLoginRequest request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

       
        if (optionalUser.isPresent()) {
            
           
            User user = optionalUser.get();
            
           
        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                    return user; 
            }
        }
        
        throw new RuntimeException("Email ou senha inválidos");
    }
    
}