package com.aithinkers.TaskHub.jwt;

import java.security.Key;
import java.util.Date;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtUtils {
    
    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;

    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;
    
    //->TaskHubImpl
    public String generateToken(String username, Integer userId) {
        return Jwts.builder()
                .subject(username)
                .claim("uid", userId)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key())
                .compact();
    }
    
    private Key key() {
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        } catch (IllegalArgumentException ex) {
            return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        }
    }
    
    //#1
 	public String getJwtFromHeader(HttpServletRequest request) {

 		String bearerToken = request.getHeader("Authorization");
 		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
 			return bearerToken.substring(7);
 		}
 		return null;
 	}

 	//#2
 	public String getUserNameFromJwtToken(String token) {
 		return Jwts
 				.parser()
 				.verifyWith((SecretKey) key())
 				.build().parseSignedClaims(token)
 				.getPayload().getSubject();
 	}

	public Integer getUserIdFromJwtToken(String token) {
		Object uid = Jwts
				.parser()
				.verifyWith((SecretKey) key())
				.build().parseSignedClaims(token)
				.getPayload().get("uid");
		if (uid == null) {
			return null;
		}
		if (uid instanceof Integer) {
			return (Integer) uid;
		}
		if (uid instanceof Number) {
			return ((Number) uid).intValue();
		}
		try {
			return Integer.parseInt(uid.toString());
		} catch (NumberFormatException ex) {
			return null;
		}
	}
 	
 	//#3
 	public boolean validateJwtToken(String authToken) {
 		try {
 			System.out.println("Validated");
 			Jwts
 			.parser()
 			.verifyWith((SecretKey) key())
 			.build()
 			.parseSignedClaims(authToken);
 			return true;
 		} catch (MalformedJwtException e) {
 		} catch (ExpiredJwtException e) {
 		} catch (UnsupportedJwtException e) {
 		} catch (IllegalArgumentException e) {
 		}
 		return false;
 	}
}
