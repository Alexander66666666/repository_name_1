package com.example.orderservice.service;

import com.example.orderservice.dto.AuthResponse;
import com.example.orderservice.entity.Role;
import com.example.orderservice.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;


    private final UserDetailsService userDetailsService;
    private final UserService userService;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }


    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof User user) {
            claims.put("role", user.getRole().name());
        }
        return generateToken(claims, userDetails, 1000 * 60 * 15); // 15 минут
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails, 1000 * 60 * 60 * 24 * 14); // 2 недели
    }


    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, long expirationTime) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public String generateToken(UserDetails userDetails) {
        return generateAccessToken(userDetails);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }


    public boolean isTokenValid(String token) {
        try {
            final String username = extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            return isTokenValid(token, userDetails);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    public AuthResponse refreshToken(String refreshToken) {
        if (!isTokenValid(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }


        String username = extractUsername(refreshToken);


        UserDetails userDetails = userDetailsService.loadUserByUsername(username);


        String newAccessToken = generateAccessToken(userDetails);
        String newRefreshToken = generateRefreshToken(userDetails);


        if (userDetails instanceof User user) {
            user.setRefreshToken(newRefreshToken);
            userService.updateUserRefreshToken(user.getId(), newRefreshToken);
        }

        Role role = (userDetails instanceof User) ? ((User) userDetails).getRole() : Role.USER;
        return new AuthResponse(newAccessToken, newRefreshToken, role);
    }
}

