package com.aithinkers.TaskHub.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import com.aithinkers.TaskHub.entity.User;
import com.aithinkers.TaskHub.repository.RegisterUserRepo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class AuthenticationTokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final RegisterUserRepo registerUserRepo;

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationTokenFilter.class);

    public AuthenticationTokenFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService, RegisterUserRepo registerUserRepo) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.registerUserRepo = registerUserRepo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain filterChain) throws ServletException, IOException {

        logger.debug("AuthenticationTokenFilter triggered for URI: {}", request.getRequestURI());

        try {
            
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                Integer userId = null;
                try { userId = jwtUtils.getUserIdFromJwtToken(jwt); } catch (Exception ignore) {}
                if (userId != null) {
                    User user = registerUserRepo.findById(userId).orElse(null);
                    if (user != null) {
                        Collection<GrantedAuthority> authorities = parseAuthorities(user.getRole());
                        UserDetails userDetails = org.springframework.security.core.userdetails.User
                                .withUsername(user.getName())
                                .password(user.getPassword())
                                .authorities(authorities)
                                .accountLocked(false)
                                .accountExpired(false)
                                .credentialsExpired(false)
                                .disabled(false)
                                .build();
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        logger.debug("Authenticated user by ID '{}', roles: {}", userId, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } else {
                	String username = jwtUtils.getUserNameFromJwtToken(jwt);
                	UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                	UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    logger.debug("Authenticated user by username '{}', roles: {}", username, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

        } catch (Exception e) {
            logger.error("Failed to set user authentication in security context: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        
        String jwt = jwtUtils.getJwtFromHeader(request);
        if (jwt == null) {
            jwt = request.getParameter("jwt_token");
        }
        
        logger.debug("Extracted JWT: {}", jwt != null ? "Token found" : "No token found");
        return jwt;
    }

    private Collection<GrantedAuthority> parseAuthorities(String roleField) {
        if (roleField == null || roleField.isBlank()) {
            return Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return Arrays.stream(roleField.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }
}
