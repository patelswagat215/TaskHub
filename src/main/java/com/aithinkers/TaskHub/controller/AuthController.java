package com.aithinkers.TaskHub.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.aithinkers.TaskHub.dto.LoginRequest;
import com.aithinkers.TaskHub.dto.LoginResponse;
import com.aithinkers.TaskHub.dto.SignUpRequest;
import com.aithinkers.TaskHub.entity.User;
import com.aithinkers.TaskHub.service.TaskHubImpl;

import lombok.RequiredArgsConstructor;

/**
 * Controller responsible for handling authentication-related requests including
 * user registration, login, and profile management
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

	@GetMapping("/home")
	public String showHomePage(@RequestParam(value = "jwt_token", required = false) String jwtToken, Model model) {
		if (jwtToken == null || jwtToken.isEmpty()) {
			return "redirect:/api/auth/login";
		}

		Integer userId = service.extractUserIdFromToken(jwtToken);
		if (userId == null) {
			return "redirect:/api/auth/login";
		}

		try {
			SignUpRequest userDetails = service.getUserDetailsForUpdate(userId);
			java.util.List<String> roles = service.getUserRolesById(userId);
			LoginResponse response = new LoginResponse(userDetails.getName(), jwtToken, roles, "Welcome back!");
			model.addAttribute("response", response);
			return "home";
		} catch (RuntimeException ex) {
			return "redirect:/api/auth/login";
		}
	}

	@GetMapping("/update")
	public String updateTheProfile(@RequestParam(value = "jwt_token", required = false) String jwtToken, Model model) {
		if (jwtToken == null || jwtToken.isEmpty()) {
			return "redirect:/api/auth/login";
		}

		Integer userId = service.extractUserIdFromToken(jwtToken);
		if (userId == null) {
			return "redirect:/api/auth/login";
		}

		try {
			SignUpRequest signUpRequest = service.getUserDetailsForUpdate(userId);
			model.addAttribute("signUpRequest", signUpRequest);
			return "viewandupdate";
		} catch (RuntimeException ex) {
			return "redirect:/api/auth/login";
		}
	}

	@PostMapping("/update")
	public String updateProfile(@RequestParam(value = "jwt_token", required = false) String jwtToken,
			SignUpRequest signUpRequest, Model model) {
		if (jwtToken == null || jwtToken.isEmpty()) {
			return "redirect:/api/auth/login";
		}

		Integer userId = service.extractUserIdFromToken(jwtToken);
		if (userId == null) {
			return "redirect:/api/auth/login";
		}

		try {
			// Get current user details to update by ID
			User currentUser = service.getUserById(userId);
			if (currentUser == null) {
				return "redirect:/api/auth/login";
			}

			String message = service.updateUserProfile(currentUser.getName(), signUpRequest);
			model.addAttribute("message", message);
			model.addAttribute("signUpRequest", signUpRequest);
			return "viewandupdate";
		} catch (RuntimeException ex) {
			model.addAttribute("error", "Update failed: " + ex.getMessage());
			model.addAttribute("signUpRequest", signUpRequest);
			return "viewandupdate";
		}
	}

	//Get all users
	@GetMapping("/getallusers")
	public String getAllUsers(Model model) {

		List<User> users = service.getAllUsers();
		model.addAttribute("users", users);
		return "userlist";
	}

	@GetMapping("/edit-role/{id}")
	public String editUserRole(@PathVariable Integer id, Model model,
			@RequestParam(value = "jwt_token", required = false) String jwtToken) {
		User user = service.getUserById(id);
		if (user == null) {
			return "redirect:/api/auth/getallusers?error=UserNotFound";
		}
		model.addAttribute("user", user);
		model.addAttribute("jwt_token", jwtToken);
		return "edit-user-role"; // create edit-user-role.html
	}

	@PostMapping("/update-role")
	public String updateUserRole(@RequestParam Integer id, @RequestParam String role,
			@RequestParam(value = "jwt_token", required = false) String jwtToken) {
		service.updateUserRole(id, role);
		String suffix = jwtToken != null ? ("?jwt_token=" + jwtToken) : "";
		return "redirect:/api/auth/getallusers" + suffix;
	}

	@GetMapping("/delete/{id}")
	public String deleteUser(@PathVariable Integer id,
			@RequestParam(value = "jwt_token", required = false) String jwtToken) {
		service.deleteUserById(id);
		String suffix = jwtToken != null ? ("?jwt_token=" + jwtToken) : "";
		return "redirect:/api/auth/getallusers" + suffix;
	}

}
