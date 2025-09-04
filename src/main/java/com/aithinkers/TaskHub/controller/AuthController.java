package com.aithinkers.TaskHub.controller;

import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.aithinkers.TaskHub.dto.LoginRequest;
import com.aithinkers.TaskHub.dto.LoginResponse;
import com.aithinkers.TaskHub.dto.SignUpRequest;
import com.aithinkers.TaskHub.service.TaskHubImpl;

import lombok.RequiredArgsConstructor;

/**
 * Controller responsible for handling authentication-related requests
 * including user registration, login, and profile management
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final TaskHubImpl service;

  
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("signupRequest", new SignUpRequest());
        return "register";
    }

    @PostMapping("/register")
    public String registerTheUser(@ModelAttribute SignUpRequest signUpRequest, Model model) {
        try {
            String message = service.registerTheUser(signUpRequest);
            model.addAttribute("message", message);
            model.addAttribute("signupRequest", new SignUpRequest());
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("signupRequest", signUpRequest); // Keep the form data
        }
        return "register";
    }


    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    @PostMapping("/home")
    public String loginTheUser(@ModelAttribute LoginRequest loginRequest, Model model) {
        try {
            // Authenticate user and generate JWT token
            LoginResponse response = service.authenticateTheUser(loginRequest);
            model.addAttribute("response", response);
            return "home";
        } catch (RuntimeException e) {
            model.addAttribute("error", "Invalid username or password");
            model.addAttribute("loginRequest", new LoginRequest());
            return "login";
        }
    }

    /**
     * Displays the welcome page for authenticated users
     * @param model Spring MVC model to add attributes
     * @param principal Current authenticated user
     * @return welcome.html template
     */
//    @GetMapping("/welcome")
//    public String showWelcomePage(Model model, Principal principal) {
//        if (principal != null) {
//            // Get user details and create a response object
//            SignUpRequest userDetails = service.getUserDetailsForUpdate(principal.getName());
//            LoginResponse response = new LoginResponse(
//                userDetails.getName(),
//                "JWT token will be available after login",
//                java.util.Arrays.asList(userDetails.getRole()),
//                "Welcome back!"
//            );
//            model.addAttribute("response", response);
//        } else {
//            // If no principal, redirect to login
//            return "redirect:/api/auth/login";
//        }
//        return "welcome";
//    }


    @GetMapping("/update")
    public String updateTheProfile(Model model, Principal principal) {
        String username = principal.getName();
        SignUpRequest signUpRequest = service.getUserDetailsForUpdate(username);
        model.addAttribute("signUpRequest", signUpRequest);
        return "viewandupdate";
    }

 
    @PostMapping("/update")
    public String updateProfile(SignUpRequest signUpRequest, Model model, Principal principal) {
        
    	    String username = principal.getName();
        String message = service.updateUserProfile(username, signUpRequest);
        model.addAttribute("message", message);
        model.addAttribute("signUpRequest",signUpRequest);
        return "viewandupdate";
    }
}
