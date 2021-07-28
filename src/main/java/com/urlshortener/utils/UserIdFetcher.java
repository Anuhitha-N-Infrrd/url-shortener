package com.urlshortener.utils;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

//This method extracts user_id from the token
@Component
public class UserIdFetcher {

    String jwtSecret="taeganger";

    private static final Logger logger = LoggerFactory.getLogger(UserIdFetcher.class);

    public String getUserId(HttpServletRequest httpServletRequest) {

        String authorizationHeader = httpServletRequest.getHeader("Authorization");
        String token = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            logger.info("-----> Token identified");
            token = authorizationHeader.substring(7);
            if(validateJwtToken(token)) {

                try {
                    Claims body = Jwts.parser()
                            .setSigningKey(jwtSecret)
                            .parseClaimsJws(token)
                            .getBody();
                    String userId = (String) body.get("sub");
                    logger.info("-----> User id extracted");
                    return userId;
                } catch (JwtException | ClassCastException e) {
                    logger.info("-----> Could not extract user_id");
                    return "JWT exception";
                }
            }
        }
        return null;
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            System.out.println(Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken).getBody().getExpiration());
            System.out.println(System.currentTimeMillis());
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
}
