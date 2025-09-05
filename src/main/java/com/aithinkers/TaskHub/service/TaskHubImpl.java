package com.aithinkers.TaskHub.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.aithinkers.TaskHub.dto.LoginRequest;
import com.aithinkers.TaskHub.dto.LoginResponse;
import com.aithinkers.TaskHub.dto.SignUpRequest;
import com.aithinkers.TaskHub.entity.User;
import com.aithinkers.TaskHub.jwt.JwtUtils;
import com.aithinkers.TaskHub.repository.RegisterUserRepo;

import lombok.RequiredArgsConstructor;

/**
 * Service implementation for TaskHub authentication and user management
 * Handles user registration, authentication, and profile updates
 */
@Service
@RequiredArgsConstructor
public class TaskHubImpl implements TaskHubService {

	private final RegisterUserRepo repo;
	private final JwtUtils jwtUtils;
	private final AuthenticationManager authenticationManager;
	private final PasswordEncoder passwordEncoder;

	/**
	 * Registers a new user in the system
	 * @param signUpRequest User registration data
	 * @return Success message with user name
	 * @throws RuntimeException if username already exists
	 */
	@Override
	public String registerTheUser(SignUpRequest signUpRequest) {
		if (repo.findByName(signUpRequest.getName()).isPresent()) {
			throw new RuntimeException("Username already exists: " + signUpRequest.getName());
		}
		if (repo.findByEmail(signUpRequest.getEmail()).isPresent()) {
			throw new RuntimeException("Email already registered: " + signUpRequest.getEmail());
		}
		
		User user = new User();
		user.setName(signUpRequest.getName());
		user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
		user.setRole("ROLE_USER");
		user.setEmail(signUpRequest.getEmail());

		repo.save(user);

		return "User " + user.getName() + " saved successfully";
	}

	/**
	 * Authenticates user and generates JWT token
	 * @param loginRequest User login credentials
	 * @return LoginResponse containing JWT token and user details
	 * @throws RuntimeException if authentication fails
	 */
	@Override
	public LoginResponse authenticateTheUser(LoginRequest loginRequest) {

	    Authentication authentication;

	    try {
	        // Authenticate user using Spring Security
	        authentication = authenticationManager.authenticate(
	                new UsernamePasswordAuthenticationToken(
	                		loginRequest.getUserName(), loginRequest.getPassword()));
	    } catch (AuthenticationException exception) {
	        throw new RuntimeException("Bad credentials");
	    }

	    // Set authentication in security context
	    SecurityContextHolder.getContext().setAuthentication(authentication);
	    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
	    
	    // Generate JWT token for the authenticated user
	    String jwtToken = jwtUtils.generateTokenFromUsername(userDetails);
	    
	    // Extract user roles
	    List<String> roles = userDetails.getAuthorities()
	            									.stream()
	            										.map(item -> item.getAuthority())
	            											.collect(Collectors.toList());

	    // Return login response with token and user details
	    return new LoginResponse(userDetails.getUsername(),jwtToken,roles,"Login successful!");
	  
	}
	
	/**
	 * Retrieves user details for profile update
	 * @param username Username of the user
	 * @return SignUpRequest object with user details
	 * @throws RuntimeException if user not found
	 */
	@Override
	public SignUpRequest getUserDetailsForUpdate(String username) {
	    User user = repo.findByName(username)
	            .orElseThrow(() -> new RuntimeException("User not found: " + username));
	    
	    // Convert User entity to SignUpRequest DTO
	    SignUpRequest signUpRequest = new SignUpRequest();
	    signUpRequest.setName(user.getName());
	    signUpRequest.setEmail(user.getEmail());
	    //signUpRequest.setRole(user.getRole());
	    signUpRequest.setPassword(user.getPassword()); 

	    return signUpRequest;
	}
	
	/**
	 * Updates user profile information
	 * @param username Username of the user to update
	 * @param signUpRequest Updated user data
	 * @return Success message
	 * @throws RuntimeException if user not found
	 */
	@Override
	public String updateUserProfile(String username, SignUpRequest signUpRequest) {
	    User user = repo.findByName(username)
	            .orElseThrow(() -> new RuntimeException("User not found"));

	    // Update user details
	    user.setName(signUpRequest.getName());
	    user.setEmail(signUpRequest.getEmail());
	    
	    // Only update password if new password is provided
	    if (signUpRequest.getPassword() != null && !signUpRequest.getPassword().isBlank()) {
	        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
	    }

	    // Save updated user to database
	    repo.save(user);
	    
	    return "Profile updated successfully!";
	}

	/**
	 * Extracts username from JWT token
	 * @param token JWT token string
	 * @return Username extracted from token
	 */
	@Override
	public String extractUsernameFromToken(String token) {
		if (token != null && jwtUtils.validateJwtToken(token)) {
			return jwtUtils.getUserNameFromJwtToken(token);
		}
		return null;
	}

	/**
	 * Debug method to get all users
	 * @return List of all users
	 */
	public List<User> getAllUsers() {
		return repo.findAll();
	}

	public User getUserById(Integer id) {
		Optional<User> user=repo.findById(id);
		return user.orElse(null);
	}

	@Override
	public String updateUserRole(Integer id, String role) {
		Optional<User> userOptional = repo.findById(id);
		if (userOptional.isEmpty()) {
			throw new RuntimeException("User not found");
		}
		User user = userOptional.get();
		user.setRole(role);
		repo.save(user);
		return "Role updated";
	}

	@Override
	public void deleteUserById(Integer id) {
		repo.deleteById(id);
	}
}
