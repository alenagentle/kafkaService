package com.irlix.Server.controllers;

import com.irlix.Server.entities.User;
import com.irlix.Server.models.KafkaResponseModel;
import com.irlix.Server.models.UserLoginModel;
import com.irlix.Server.models.UserRegistrationModel;
import com.irlix.Server.repositories.UserRepository;
import com.irlix.Server.services.MyMileService;
import com.irlix.Server.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping
@CrossOrigin(origins = "*", maxAge = 3600)
public class MainController {


    private AuthenticationManager authenticationManager;
    private UserRepository userRepository;
    private UserService userService;
    private MyMileService myMileService;

    @Autowired
    public MainController(AuthenticationManager authenticationManager, UserRepository userRepository, MyMileService myMileService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.myMileService = myMileService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String homePage(){
        return "home1";
    }

    @PostMapping("/registration")
    public ResponseEntity registration(@RequestBody UserRegistrationModel userRegistration){
//        System.out.println("we are on /registration");
        if(this.userRepository.existsUserByLogin(userRegistration.getLogin()))
        {
            System.out.println("Такой пользователь уже зарегистрирован");
            return  ResponseEntity.badRequest().body("Такой пользователь уже зарегистрирован");
        }
        KafkaResponseModel kafkaResponse = new KafkaResponseModel();


        try {
            kafkaResponse = userService.sendValidatePassword(userRegistration);
        } catch (InterruptedException e) {
            System.out.println("InterruptedException : " + e.getMessage());
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            System.out.println("ExecutionException : " + e.getMessage());
            e.printStackTrace();
        } catch (TimeoutException e) {
            System.out.println("TimeoutException : " + e.getMessage());
            e.printStackTrace();
        }

        if(kafkaResponse.getValidationState())
        {
            userService.register(userRegistration);
            String message = userRegistration.getFio() + ", Ваша учетная запись зарегистрирована.";
            myMileService.sendSimpleEmail(userRegistration.getEmail(), "Регистрация", message);
            kafkaResponse.setDescription(kafkaResponse.getDescription() + " Пользователь зарегистриован.");
            return ResponseEntity.ok().body(kafkaResponse.getDescription());

        }
        else
            return ResponseEntity.badRequest().body(kafkaResponse.getDescription());
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authUser(@RequestBody UserLoginModel userLogin) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userLogin.getLogin(),
                        userLogin.getPassword()
                ));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return ResponseEntity.ok("Здравствуйте, " + userDetails.getUsername());
    }

    @GetMapping("/filter/{str}")
    public ResponseEntity<List<User>> filter(@PathVariable(value="str") String str){
        System.out.println("str = " + str);
        return ResponseEntity.ok(userService.getUsersByStringFilter(str));
    }

    @GetMapping("/authenticated")
    public String homepage(){
        System.out.println("Зашли на homepage");
        return "home2";
    }

}
