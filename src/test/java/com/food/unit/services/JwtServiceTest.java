package com.food.unit.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.food.services.JwtService;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private String validToken;
    private String invalidToken = "invalid.token.here";
    
    @BeforeEach
    void setUp() {
        validToken = jwtService.generateToken("testUser");
    }

    @Test
    void testGenerateToken() {
        assertNotNull(validToken);
        assertFalse(validToken.isEmpty());
    }

    @Test
    void testValidateToken_Valid() {
        String username = jwtService.validateToken(validToken);
        assertEquals("testUser", username);
    }

    @Test
    void testValidateToken_Invalid() {
        String username = jwtService.validateToken(invalidToken);
        assertNull(username);
    }

    @Test
    void testValidateToken_Expired() {
        JwtService expiredJwtService = new JwtService() {
            @Override
            public String generateToken(String username) {
                Date now = new Date();
                Date expiryDate = new Date(now.getTime() - 1000); 
                return io.jsonwebtoken.Jwts.builder()
                        .subject(username)
                        .issuedAt(now)
                        .expiration(expiryDate)
                        .signWith(SECRET_KEY)
                        .compact();
            }
        };

        String expiredToken = expiredJwtService.generateToken("testUser");
        assertNull(expiredJwtService.validateToken(expiredToken));
    }
}
