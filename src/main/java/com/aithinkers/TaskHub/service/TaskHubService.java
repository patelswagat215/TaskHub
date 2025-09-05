package com.aithinkers.TaskHub.service;

import java.util.List;
import org.springframework.http.ResponseEntity;

import com.aithinkers.TaskHub.dto.LoginRequest;
import com.aithinkers.TaskHub.dto.LoginResponse;
import com.aithinkers.TaskHub.dto.SignUpRequest;
import com.aithinkers.TaskHub.entity.User;

public interface TaskHubService {
	
	String registerTheUser(SignUpRequest signUpRequest);
	LoginResponse authenticateTheUser(LoginRequest loginRequest);
	SignUpRequest getUserDetailsForUpdate(String username);
	String updateUserProfile(String username, SignUpRequest signUpRequest);
	String extractUsernameFromToken(String token);
	List<User> getAllUsers();
	User getUserById(Integer id);
	String updateUserRole(Integer id, String role);
	void deleteUserById(Integer id);

}
